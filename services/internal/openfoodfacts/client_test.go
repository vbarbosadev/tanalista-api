package openfoodfacts

import (
	"context"
	"errors"
	"fmt"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/vbarbosadev/tanalista-api/services/internal/barcode"
)

func TestHTTPContract(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path != "/off/api/v2/product/012345678905.json" ||
			r.Header.Get("User-Agent") != "TaNaLista/test (test@example.invalid)" ||
			r.Header.Get("Accept") != "application/json" ||
			r.URL.Query().Get("fields") != "code,product_name,brands,categories,product_quantity_unit" {
			t.Errorf("wrong request: %s %v", r.URL, r.Header)
		}
		fmt.Fprint(w, `{"status":1,"code":"0012345678905","product":{"product_name":" Leite  integral ","brands":" Marca A ","categories":" Laticínios ","product_quantity_unit":"ml"}}`)
	}))
	defer srv.Close()
	client, err := New(srv.URL+"/off", "TaNaLista/test (test@example.invalid)", time.Second)
	if err != nil {
		t.Fatal(err)
	}
	p, err := barcode.New(client).Lookup(context.Background(), "012345678905")
	if err != nil {
		t.Fatal(err)
	}
	if p.Name != "Leite integral" || p.Brand != "Marca A" || p.Category != "Laticínios" ||
		p.Unit != "ml" || p.Source != "open-food-facts" || p.Code != "012345678905" {
		t.Fatalf("unexpected product: %+v", p)
	}
}

func TestFailures(t *testing.T) {
	for _, tc := range []struct {
		name string
		code int
		body string
		want error
	}{
		{"absent HTTP", 404, `{"status":0}`, barcode.ErrNotFound},
		{"absent envelope", 200, `{"status":0}`, barcode.ErrNotFound},
		{"unavailable", 503, `{"error":"unavailable"}`, barcode.ErrUnavailable},
		{"rate limited", 429, `{}`, barcode.ErrUnavailable},
		{"HTML proxy", 500, "<html>error</html>", barcode.ErrUnavailable},
		{"HTML success", 200, "<html>error</html>", barcode.ErrUnavailable},
		{"missing status", 200, `{"product":{"product_name":"Leite"}}`, barcode.ErrUnavailable},
		{"missing product", 200, `{"status":1}`, barcode.ErrUnavailable},
		{"unknown status", 200, `{"status":2,"product":{}}`, barcode.ErrUnavailable},
		{"invalid JSON", 200, `{`, barcode.ErrUnavailable},
		{"wrong code", 200, `{"status":1,"code":"96385074","product":{"product_name":"Leite"}}`, barcode.ErrUnavailable},
		{"no usable name", 200, `{"status":1,"product":{"product_name":"  "}}`, barcode.ErrNotFound},
		{"oversized", 200, strings.Repeat(" ", maxBody+1), barcode.ErrUnavailable},
	} {
		t.Run(tc.name, func(t *testing.T) {
			srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
				w.WriteHeader(tc.code)
				fmt.Fprint(w, tc.body)
			}))
			defer srv.Close()
			client, _ := New(srv.URL, "TaNaLista/test", time.Second)
			_, err := client.Lookup(context.Background(), "3017624010701")
			if !errors.Is(err, tc.want) {
				t.Fatalf("got %v, want %v", err, tc.want)
			}
		})
	}
}

func TestOptionalValuesAreNotInvented(t *testing.T) {
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
		fmt.Fprint(w, `{"status":1,"product":{"product_name":"Produto","product_quantity_unit":"UNIDADE"}}`)
	}))
	defer srv.Close()
	client, _ := New(srv.URL, "TaNaLista/test", time.Second)
	p, err := barcode.New(client).Lookup(context.Background(), "3017624010701")
	if err != nil || p.Brand != "" || p.Category != "" || p.Unit != "" {
		t.Fatalf("%+v %v", p, err)
	}
}

func TestDeadlineAndCancellationStopHTTP(t *testing.T) {
	for _, useDeadline := range []bool{false, true} {
		t.Run(fmt.Sprint(useDeadline), func(t *testing.T) {
			started, stopped := make(chan struct{}), make(chan struct{})
			srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
				close(started)
				<-r.Context().Done()
				close(stopped)
			}))
			defer srv.Close()
			client, _ := New(srv.URL, "TaNaLista/test", time.Second)
			ctx, cancel := context.WithCancel(context.Background())
			want := context.Canceled
			if useDeadline {
				cancel()
				ctx, cancel = context.WithTimeout(context.Background(), 100*time.Millisecond)
				want = context.DeadlineExceeded
			}
			defer cancel()
			result := make(chan error, 1)
			go func() {
				_, err := client.Lookup(ctx, "3017624010701")
				result <- err
			}()
			select {
			case <-started:
			case <-time.After(2 * time.Second):
				t.Fatal("request did not reach HTTP server")
			}
			if !useDeadline {
				cancel()
			}
			select {
			case err := <-result:
				if !errors.Is(err, want) {
					t.Fatalf("got %v want %v", err, want)
				}
			case <-time.After(2 * time.Second):
				t.Fatal("HTTP did not stop")
			}
			select {
			case <-stopped:
			case <-time.After(2 * time.Second):
				t.Fatal("server did not observe cancellation")
			}
		})
	}
}

func TestUnreachableAndClientTimeout(t *testing.T) {
	dead := httptest.NewServer(http.HandlerFunc(func(http.ResponseWriter, *http.Request) {}))
	addr := dead.URL
	dead.Close()
	client, _ := New(addr, "TaNaLista/test", time.Second)
	if _, err := client.Lookup(context.Background(), "3017624010701"); !errors.Is(err, barcode.ErrUnavailable) {
		t.Fatal(err)
	}
	srv := httptest.NewServer(http.HandlerFunc(func(_ http.ResponseWriter, r *http.Request) { <-r.Context().Done() }))
	defer srv.Close()
	client, _ = New(srv.URL, "TaNaLista/test", 20*time.Millisecond)
	if _, err := client.Lookup(context.Background(), "3017624010701"); !errors.Is(err, context.DeadlineExceeded) {
		t.Fatal(err)
	}
}

func TestConfigurationAndNoRedirect(t *testing.T) {
	for _, addr := range []string{"", "file:///tmp/x", "http://u:p@localhost", "https://example.com?q=x", "https://example.com#x"} {
		if _, err := New(addr, "TaNaLista/test", time.Second); err == nil {
			t.Fatal("invalid base accepted:", addr)
		}
	}
	if _, err := New("https://example.com", "", time.Second); err == nil {
		t.Fatal("missing user agent accepted")
	}
	target := httptest.NewServer(http.HandlerFunc(func(http.ResponseWriter, *http.Request) {
		t.Error("redirect was followed")
	}))
	defer target.Close()
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		http.Redirect(w, r, target.URL, http.StatusFound)
	}))
	defer srv.Close()
	client, _ := New(srv.URL, "TaNaLista/test", time.Second)
	if _, err := client.Lookup(context.Background(), "3017624010701"); !errors.Is(err, barcode.ErrUnavailable) {
		t.Fatal(err)
	}
}
