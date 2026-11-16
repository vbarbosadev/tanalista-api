package grpcserver

import (
	"context"
	"errors"
	"net"
	"testing"
	"time"

	barcodev1 "github.com/vbarbosadev/tanalista-api/services/gen/tanalista/barcode/v1"
	"github.com/vbarbosadev/tanalista-api/services/internal/barcode"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials/insecure"
	healthv1 "google.golang.org/grpc/health/grpc_health_v1"
	reflectionv1 "google.golang.org/grpc/reflection/grpc_reflection_v1"
	"google.golang.org/grpc/status"
	"google.golang.org/grpc/test/bufconn"
)

type lookupFunc func(context.Context, string) (barcode.Product, error)

func (f lookupFunc) Lookup(ctx context.Context, code string) (barcode.Product, error) {
	return f(ctx, code)
}

func connection(t *testing.T, lookup Lookup) *grpc.ClientConn {
	t.Helper()
	listener := bufconn.Listen(1024 * 1024)
	server, h := New(lookup, 500*time.Millisecond)
	go func() {
		if err := server.Serve(listener); err != nil && !errors.Is(err, grpc.ErrServerStopped) {
			t.Errorf("serve: %v", err)
		}
	}()
	conn, err := grpc.NewClient("passthrough:///bufnet",
		grpc.WithContextDialer(func(ctx context.Context, _ string) (net.Conn, error) { return listener.DialContext(ctx) }),
		grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		t.Fatal(err)
	}
	t.Cleanup(func() {
		conn.Close()
		Stop(server, h, time.Second)
		listener.Close()
	})
	return conn
}

func TestGRPCMappings(t *testing.T) {
	for _, tc := range []struct {
		err  error
		code codes.Code
	}{
		{barcode.ErrInvalidCode, codes.InvalidArgument},
		{barcode.ErrNotFound, codes.NotFound},
		{barcode.ErrUnavailable, codes.Unavailable},
		{context.DeadlineExceeded, codes.DeadlineExceeded},
		{context.Canceled, codes.Canceled},
		{errors.New("sensitive detail"), codes.Internal},
	} {
		t.Run(tc.code.String(), func(t *testing.T) {
			conn := connection(t, lookupFunc(func(context.Context, string) (barcode.Product, error) {
				return barcode.Product{}, tc.err
			}))
			ctx, cancel := context.WithTimeout(context.Background(), time.Second)
			defer cancel()
			_, err := barcodev1.NewBarcodeServiceClient(conn).LookupProduct(ctx, &barcodev1.LookupProductRequest{Code: "3017624010701"})
			if status.Code(err) != tc.code {
				t.Fatalf("got %v want %v", err, tc.code)
			}
			if status.Convert(err).Message() == "sensitive detail" {
				t.Fatal("internal error leaked")
			}
		})
	}
}

func TestSuccessHealthAndReflection(t *testing.T) {
	conn := connection(t, lookupFunc(func(_ context.Context, code string) (barcode.Product, error) {
		return barcode.Product{Code: code, Name: "Produto", Source: "open-food-facts"}, nil
	}))
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()
	res, err := barcodev1.NewBarcodeServiceClient(conn).LookupProduct(ctx, &barcodev1.LookupProductRequest{Code: "012345678905"})
	if err != nil || res.GetCode() != "012345678905" || res.Brand != nil || res.Unit != nil {
		t.Fatalf("%v %v", res, err)
	}
	h, err := healthv1.NewHealthClient(conn).Check(ctx, &healthv1.HealthCheckRequest{})
	if err != nil || h.GetStatus() != healthv1.HealthCheckResponse_SERVING {
		t.Fatalf("%v %v", h, err)
	}
	stream, err := reflectionv1.NewServerReflectionClient(conn).ServerReflectionInfo(ctx)
	if err != nil {
		t.Fatal(err)
	}
	defer stream.CloseSend()
	if err := stream.Send(&reflectionv1.ServerReflectionRequest{
		MessageRequest: &reflectionv1.ServerReflectionRequest_ListServices{ListServices: ""},
	}); err != nil {
		t.Fatal(err)
	}
	result, err := stream.Recv()
	if err != nil {
		t.Fatal(err)
	}
	found := false
	for _, svc := range result.GetListServicesResponse().GetService() {
		found = found || svc.Name == "tanalista.barcode.v1.BarcodeService"
	}
	if !found {
		t.Fatal("reflection missing application service")
	}
}

func TestDeadlineStopsWork(t *testing.T) {
	stopped := make(chan struct{})
	conn := connection(t, lookupFunc(func(ctx context.Context, _ string) (barcode.Product, error) {
		defer close(stopped)
		<-ctx.Done()
		return barcode.Product{}, ctx.Err()
	}))
	ctx, cancel := context.WithTimeout(context.Background(), 100*time.Millisecond)
	defer cancel()
	_, err := barcodev1.NewBarcodeServiceClient(conn).LookupProduct(ctx, &barcodev1.LookupProductRequest{Code: "3017624010701"})
	if status.Code(err) != codes.DeadlineExceeded {
		t.Fatal(err)
	}
	select {
	case <-stopped:
	case <-time.After(time.Second):
		t.Fatal("domain did not observe cancellation")
	}
}

func TestShutdownIsBoundedEvenForUncooperativeHandler(t *testing.T) {
	started, release := make(chan struct{}), make(chan struct{})
	listener := bufconn.Listen(1024 * 1024)
	defer listener.Close()
	server, h := New(lookupFunc(func(context.Context, string) (barcode.Product, error) {
		close(started)
		<-release
		return barcode.Product{}, context.Canceled
	}), time.Second)
	go server.Serve(listener)
	defer server.Stop()
	defer close(release)
	conn, err := grpc.NewClient("passthrough:///shutdown",
		grpc.WithContextDialer(func(ctx context.Context, _ string) (net.Conn, error) { return listener.DialContext(ctx) }),
		grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		t.Fatal(err)
	}
	defer conn.Close()
	ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
	defer cancel()
	go barcodev1.NewBarcodeServiceClient(conn).LookupProduct(ctx, &barcodev1.LookupProductRequest{Code: "3017624010701"})
	select {
	case <-started:
	case <-time.After(time.Second):
		t.Fatal("handler did not start")
	}
	stopped := make(chan struct{})
	go func() {
		Stop(server, h, 20*time.Millisecond)
		close(stopped)
	}()
	select {
	case <-stopped:
	case <-time.After(time.Second):
		t.Fatal("shutdown exceeded grace period")
	}
}
