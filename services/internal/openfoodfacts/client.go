// Package openfoodfacts adapts the provider's v2 HTTP API to the barcode port.
package openfoodfacts

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"time"

	"github.com/vbarbosadev/tanalista-api/services/internal/barcode"
)

const maxBody = 1 << 20

type Client struct {
	base      *url.URL
	userAgent string
	http      *http.Client
}

func New(baseURL, userAgent string, timeout time.Duration) (*Client, error) {
	base, err := url.Parse(baseURL)
	if err != nil || base == nil || base.Host == "" || (base.Scheme != "http" && base.Scheme != "https") ||
		base.User != nil || base.RawQuery != "" || base.Fragment != "" {
		return nil, errors.New("invalid provider base URL")
	}
	if strings.TrimSpace(userAgent) == "" || strings.ContainsAny(userAgent, "\r\n") || timeout <= 0 {
		return nil, errors.New("user agent and positive timeout required")
	}
	return &Client{base: base, userAgent: userAgent, http: &http.Client{
		Timeout: timeout,
		CheckRedirect: func(*http.Request, []*http.Request) error {
			return http.ErrUseLastResponse
		},
	}}, nil
}

func unavailable(detail string) error {
	return fmt.Errorf("%w: %s", barcode.ErrUnavailable, detail)
}

func (c *Client) Lookup(ctx context.Context, code string) (barcode.Product, error) {
	if err := ctx.Err(); err != nil {
		return barcode.Product{}, err
	}
	if err := barcode.ValidateCode(code); err != nil {
		return barcode.Product{}, err
	}
	endpoint := *c.base
	endpoint.Path = strings.TrimRight(endpoint.Path, "/") + "/api/v2/product/" + code + ".json"
	query := endpoint.Query()
	query.Set("fields", "code,product_name,brands,categories,product_quantity_unit")
	endpoint.RawQuery = query.Encode()
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, endpoint.String(), nil)
	if err != nil {
		return barcode.Product{}, unavailable("request could not be created")
	}
	req.Header.Set("Accept", "application/json")
	req.Header.Set("User-Agent", c.userAgent)
	res, err := c.http.Do(req)
	if err != nil {
		if ctx.Err() != nil {
			return barcode.Product{}, fmt.Errorf("provider request: %w", ctx.Err())
		}
		if errors.Is(err, context.DeadlineExceeded) {
			return barcode.Product{}, fmt.Errorf("provider timeout: %w", context.DeadlineExceeded)
		}
		return barcode.Product{}, unavailable("request failed")
	}
	defer res.Body.Close()
	if res.StatusCode == http.StatusNotFound {
		return barcode.Product{}, barcode.ErrNotFound
	}
	if res.StatusCode != http.StatusOK {
		return barcode.Product{}, unavailable(fmt.Sprintf("HTTP %d", res.StatusCode))
	}
	body, err := io.ReadAll(io.LimitReader(res.Body, maxBody+1))
	if err != nil {
		if ctx.Err() != nil {
			return barcode.Product{}, ctx.Err()
		}
		if errors.Is(err, context.DeadlineExceeded) {
			return barcode.Product{}, context.DeadlineExceeded
		}
		return barcode.Product{}, unavailable("body read failed")
	}
	if len(body) > maxBody {
		return barcode.Product{}, unavailable("body exceeds limit")
	}
	var data struct {
		Status  *int   `json:"status"`
		Code    string `json:"code"`
		Product *struct {
			Name     string `json:"product_name"`
			Brands   string `json:"brands"`
			Category string `json:"categories"`
			Unit     string `json:"product_quantity_unit"`
		} `json:"product"`
	}
	if err := json.Unmarshal(body, &data); err != nil || data.Status == nil {
		return barcode.Product{}, unavailable("invalid JSON envelope")
	}
	if *data.Status == 0 {
		return barcode.Product{}, barcode.ErrNotFound
	}
	if *data.Status != 1 || data.Product == nil {
		return barcode.Product{}, unavailable("invalid product envelope")
	}
	if data.Code != "" && strings.TrimLeft(data.Code, "0") != strings.TrimLeft(code, "0") {
		return barcode.Product{}, unavailable("response barcode mismatch")
	}
	if strings.TrimSpace(data.Product.Name) == "" {
		return barcode.Product{}, barcode.ErrNotFound
	}
	unit := strings.TrimSpace(data.Product.Unit)
	if unit != "g" && unit != "ml" {
		unit = ""
	}
	return barcode.Product{
		Code: code, Name: data.Product.Name, Brand: data.Product.Brands,
		Category: data.Product.Category, Unit: unit, Source: "open-food-facts",
	}, nil
}
