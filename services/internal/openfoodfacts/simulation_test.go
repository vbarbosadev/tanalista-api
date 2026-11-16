package openfoodfacts

import (
	"context"
	"errors"
	"os"
	"testing"
	"time"

	"github.com/vbarbosadev/tanalista-api/services/internal/barcode"
)

// TestSimulation executes the production HTTP adapter against Postman, not a
// stubbed RoundTripper. It is opt-in and makes no calls to real providers.
func TestSimulation(t *testing.T) {
	base := os.Getenv("OFF_SIM_URL")
	if base == "" {
		t.Skip("start postman simulation and set OFF_SIM_URL")
	}
	client, err := New(base, "TaNaLista/simulation (https://github.com/vbarbosadev/tanalista-api)", time.Second)
	if err != nil {
		t.Fatal(err)
	}
	service := barcode.New(client)
	p, err := service.Lookup(context.Background(), "3017624010701")
	if err != nil || p.Name != "Produto de teste" || p.Unit != "g" {
		t.Fatalf("success path did not execute: %+v %v", p, err)
	}
	for _, test := range []struct {
		code string
		want error
	}{
		{"96385074", barcode.ErrNotFound},
		{"012345678905", barcode.ErrUnavailable},
		{"4006381333931", barcode.ErrUnavailable},
	} {
		if _, err := service.Lookup(context.Background(), test.code); !errors.Is(err, test.want) {
			t.Fatalf("%s: %v want %v", test.code, err, test.want)
		}
	}
	ctx, cancel := context.WithTimeout(context.Background(), 100*time.Millisecond)
	defer cancel()
	if _, err := service.Lookup(ctx, "5901234123457"); !errors.Is(err, context.DeadlineExceeded) {
		t.Fatalf("slow dependency: %v", err)
	}
}
