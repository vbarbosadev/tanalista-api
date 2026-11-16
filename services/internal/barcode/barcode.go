// Package barcode contains the read-only product lookup domain, without transport or storage.
package barcode

import (
	"context"
	"errors"
	"fmt"
	"strings"
)

var (
	ErrInvalidCode = errors.New("invalid barcode")
	ErrNotFound    = errors.New("product not found")
	ErrUnavailable = errors.New("provider unavailable")
)

// Product contains suggestions. Empty optional labels mean unknown, not a default.
type Product struct {
	Code, Name, Brand, Category, Unit, Source string
}

// Provider supplies metadata and must respect the caller's cancellation/deadline.
// It must not persist products or create shopping-list items.
type Provider interface {
	Lookup(ctx context.Context, code string) (Product, error)
}

// Service validates requests and enforces a usable normalized result.
type Service struct {
	provider Provider
}

func New(provider Provider) *Service {
	return &Service{provider: provider}
}

// ValidateCode accepts EAN-8, UPC-A and EAN-13, without stripping leading zeroes.
func ValidateCode(code string) error {
	if len(code) != 8 && len(code) != 12 && len(code) != 13 {
		return fmt.Errorf("%w: expected 8, 12 or 13 ASCII digits", ErrInvalidCode)
	}
	sum := 0
	for i := range len(code) {
		if code[i] < '0' || code[i] > '9' {
			return fmt.Errorf("%w: expected ASCII digits", ErrInvalidCode)
		}
		if i < len(code)-1 {
			weight := 1
			if (len(code)-2-i)%2 == 0 {
				weight = 3
			}
			sum += int(code[i]-'0') * weight
		}
	}
	if (10-sum%10)%10 != int(code[len(code)-1]-'0') {
		return fmt.Errorf("%w: check digit does not match", ErrInvalidCode)
	}
	return nil
}

func normalize(value string) string {
	return strings.Join(strings.Fields(value), " ")
}

// Lookup never invents defaults or silently converts failures into successful empty results.
func (s *Service) Lookup(ctx context.Context, code string) (Product, error) {
	if err := ctx.Err(); err != nil {
		return Product{}, fmt.Errorf("lookup cancelled: %w", err)
	}
	if err := ValidateCode(code); err != nil {
		return Product{}, err
	}
	if s.provider == nil {
		return Product{}, ErrUnavailable
	}
	product, err := s.provider.Lookup(ctx, code)
	// A late provider result must not override caller cancellation.
	if ctxErr := ctx.Err(); ctxErr != nil {
		return Product{}, fmt.Errorf("lookup interrupted: %w", ctxErr)
	}
	if err != nil {
		return Product{}, fmt.Errorf("lookup provider: %w", err)
	}
	product.Code = code
	product.Name = normalize(product.Name)
	product.Brand = normalize(product.Brand)
	product.Category = normalize(product.Category)
	product.Unit = normalize(product.Unit)
	product.Source = normalize(product.Source)
	if product.Name == "" {
		return Product{}, ErrNotFound
	}
	if product.Source == "" {
		return Product{}, fmt.Errorf("%w: missing source", ErrUnavailable)
	}
	return product, nil
}
