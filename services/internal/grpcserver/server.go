// Package grpcserver translates the generated transport contract to the domain.
package grpcserver

import (
	"context"
	"errors"
	"time"

	barcodev1 "github.com/vbarbosadev/tanalista-api/services/gen/tanalista/barcode/v1"
	"github.com/vbarbosadev/tanalista-api/services/internal/barcode"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/health"
	healthv1 "google.golang.org/grpc/health/grpc_health_v1"
	"google.golang.org/grpc/reflection"
	"google.golang.org/grpc/status"
)

type Lookup interface {
	Lookup(context.Context, string) (barcode.Product, error)
}

type Adapter struct {
	barcodev1.UnimplementedBarcodeServiceServer
	lookup Lookup
}

func optional(value string) *string {
	if value == "" {
		return nil
	}
	return &value
}

func (a *Adapter) LookupProduct(ctx context.Context, req *barcodev1.LookupProductRequest) (*barcodev1.LookupProductResponse, error) {
	if req == nil {
		return nil, status.Error(codes.InvalidArgument, "barcode required")
	}
	p, err := a.lookup.Lookup(ctx, req.GetCode())
	if err != nil {
		switch {
		case errors.Is(err, context.DeadlineExceeded):
			return nil, status.Error(codes.DeadlineExceeded, "lookup deadline exceeded")
		case errors.Is(err, context.Canceled):
			return nil, status.Error(codes.Canceled, "lookup cancelled")
		case errors.Is(err, barcode.ErrInvalidCode):
			return nil, status.Error(codes.InvalidArgument, "invalid barcode")
		case errors.Is(err, barcode.ErrNotFound):
			return nil, status.Error(codes.NotFound, "product not found")
		case errors.Is(err, barcode.ErrUnavailable):
			return nil, status.Error(codes.Unavailable, "provider unavailable")
		default:
			return nil, status.Error(codes.Internal, "lookup failed")
		}
	}
	return &barcodev1.LookupProductResponse{
		Code: p.Code, Name: p.Name, Brand: optional(p.Brand), Category: optional(p.Category),
		Unit: optional(p.Unit), Source: p.Source,
	}, nil
}

// New registers the application, health and reflection. A server-side cap also
// protects against clients that omit deadlines; a shorter caller deadline wins.
func New(lookup Lookup, maxDuration time.Duration) (*grpc.Server, *health.Server) {
	server := grpc.NewServer(
		grpc.MaxRecvMsgSize(4096),
		grpc.UnaryInterceptor(func(ctx context.Context, req any, _ *grpc.UnaryServerInfo, handler grpc.UnaryHandler) (any, error) {
			ctx, cancel := context.WithTimeout(ctx, maxDuration)
			defer cancel()
			return handler(ctx, req)
		}),
	)
	barcodev1.RegisterBarcodeServiceServer(server, &Adapter{lookup: lookup})
	h := health.NewServer()
	h.SetServingStatus("", healthv1.HealthCheckResponse_SERVING)
	h.SetServingStatus(barcodev1.BarcodeService_ServiceDesc.ServiceName, healthv1.HealthCheckResponse_SERVING)
	healthv1.RegisterHealthServer(server, h)
	reflection.Register(server)
	return server, h
}

// Stop drains active calls but bounds shutdown if a client does not cooperate.
func Stop(server *grpc.Server, h *health.Server, grace time.Duration) {
	h.Shutdown()
	done := make(chan struct{})
	go func() {
		server.GracefulStop()
		close(done)
	}()
	timer := time.NewTimer(grace)
	defer timer.Stop()
	select {
	case <-done:
	case <-timer.C:
		// GracefulStop may hold gRPC's internal lock waiting for a handler.
		// Never let a second synchronous Stop prevent the process from exiting.
		go server.Stop()
	}
}
