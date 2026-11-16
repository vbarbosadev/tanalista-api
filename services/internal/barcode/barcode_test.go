package barcode

import (
	"context"
	"errors"
	"fmt"
	"testing"
	"time"
)

type providerFunc func(context.Context, string) (Product, error)

func (f providerFunc) Lookup(ctx context.Context, code string) (Product, error) {
	return f(ctx, code)
}

func TestValidateCode(t *testing.T) {
	for _, code := range []string{"96385074", "012345678905", "3017624010701"} {
		t.Run(code, func(t *testing.T) {
			if err := ValidateCode(code); err != nil {
				t.Fatal(err)
			}
		})
	}
	for _, code := range []string{"", " 3017624010701", "3017624010701 ", "3017624010702",
		"96385075", "012345678906", "123456", "12345678901234", "１２３４５６７８", "301762401070A"} {
		t.Run("invalid/"+code, func(t *testing.T) {
			if !errors.Is(ValidateCode(code), ErrInvalidCode) {
				t.Fatal("invalid code accepted")
			}
		})
	}
}

func TestInvalidDoesNotCallProvider(t *testing.T) {
	s := New(providerFunc(func(context.Context, string) (Product, error) {
		t.Fatal("invalid input reached provider")
		return Product{}, nil
	}))
	if _, err := s.Lookup(context.Background(), "bad"); !errors.Is(err, ErrInvalidCode) {
		t.Fatal(err)
	}
}

func TestNormalizeWithoutInventingOptionalFields(t *testing.T) {
	s := New(providerFunc(func(_ context.Context, code string) (Product, error) {
		if code != "012345678905" {
			t.Fatal("leading zero lost")
		}
		return Product{Name: "  Leite\n integral ", Brand: " Marca  A ", Source: " open-food-facts "}, nil
	}))
	p, err := s.Lookup(context.Background(), "012345678905")
	if err != nil {
		t.Fatal(err)
	}
	if p.Code != "012345678905" || p.Name != "Leite integral" || p.Brand != "Marca A" ||
		p.Category != "" || p.Unit != "" || p.Source != "open-food-facts" {
		t.Fatalf("unexpected normalization: %+v", p)
	}
}

func TestProviderErrorsRemainRecognizable(t *testing.T) {
	for _, original := range []error{ErrNotFound, ErrUnavailable, context.DeadlineExceeded, context.Canceled} {
		t.Run(original.Error(), func(t *testing.T) {
			s := New(providerFunc(func(context.Context, string) (Product, error) {
				return Product{}, fmt.Errorf("adapter: %w", original)
			}))
			if _, err := s.Lookup(context.Background(), "3017624010701"); !errors.Is(err, original) {
				t.Fatal(err)
			}
		})
	}
}

func TestUnusableProductAndMissingProvider(t *testing.T) {
	for _, test := range []struct {
		name string
		p    Product
		err  error
	}{
		{"no name", Product{Source: "open-food-facts"}, ErrNotFound},
		{"no source", Product{Name: "Produto"}, ErrUnavailable},
	} {
		t.Run(test.name, func(t *testing.T) {
			s := New(providerFunc(func(context.Context, string) (Product, error) { return test.p, nil }))
			if _, err := s.Lookup(context.Background(), "3017624010701"); !errors.Is(err, test.err) {
				t.Fatal(err)
			}
		})
	}
	if _, err := New(nil).Lookup(context.Background(), "3017624010701"); !errors.Is(err, ErrUnavailable) {
		t.Fatal(err)
	}
}

func TestCancellationBeforeCall(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	cancel()
	s := New(providerFunc(func(context.Context, string) (Product, error) {
		t.Fatal("cancelled request reached provider")
		return Product{}, nil
	}))
	if _, err := s.Lookup(ctx, "3017624010701"); !errors.Is(err, context.Canceled) {
		t.Fatal(err)
	}
}

func TestDeadlineInterruptsWaiting(t *testing.T) {
	ctx, cancel := context.WithTimeout(context.Background(), 25*time.Millisecond)
	defer cancel()
	stopped := make(chan struct{})
	s := New(providerFunc(func(ctx context.Context, _ string) (Product, error) {
		defer close(stopped)
		<-ctx.Done()
		return Product{}, ctx.Err()
	}))
	start := time.Now()
	_, err := s.Lookup(ctx, "3017624010701")
	if !errors.Is(err, context.DeadlineExceeded) {
		t.Fatal(err)
	}
	if time.Since(start) > time.Second {
		t.Fatal("deadline not respected")
	}
	select {
	case <-stopped:
	default:
		t.Fatal("provider work did not stop")
	}
}

func TestLateSuccessDoesNotOverrideCancellation(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	s := New(providerFunc(func(context.Context, string) (Product, error) {
		cancel()
		return Product{Name: "Produto", Source: "open-food-facts"}, nil
	}))
	if _, err := s.Lookup(ctx, "3017624010701"); !errors.Is(err, context.Canceled) {
		t.Fatal("late success overrode cancellation:", err)
	}
}
