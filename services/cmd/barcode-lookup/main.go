package main

import (
	"context"
	"errors"
	"flag"
	"fmt"
	"log"
	"net"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/vbarbosadev/tanalista-api/services/internal/barcode"
	"github.com/vbarbosadev/tanalista-api/services/internal/grpcserver"
	"github.com/vbarbosadev/tanalista-api/services/internal/openfoodfacts"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	healthv1 "google.golang.org/grpc/health/grpc_health_v1"
)

func env(key, fallback string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return fallback
}

func checkHealth() error {
	conn, err := grpc.NewClient(env("BARCODE_HEALTH_ADDR", "127.0.0.1:9090"),
		grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		return err
	}
	defer conn.Close()
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()
	res, err := healthv1.NewHealthClient(conn).Check(ctx, &healthv1.HealthCheckRequest{})
	if err != nil {
		return err
	}
	if res.Status != healthv1.HealthCheckResponse_SERVING {
		return errors.New("service not serving")
	}
	return nil
}

func run() error {
	healthcheck := flag.Bool("healthcheck", false, "query gRPC health and exit")
	flag.Parse()
	if *healthcheck {
		return checkHealth()
	}
	timeout, err := time.ParseDuration(env("OFF_TIMEOUT", "2s"))
	if err != nil || timeout <= 0 {
		return errors.New("OFF_TIMEOUT must be a positive duration")
	}
	provider, err := openfoodfacts.New(env("OFF_BASE_URL", "https://world.openfoodfacts.org"),
		env("OFF_USER_AGENT", "TaNaLista/1.0 (https://github.com/vbarbosadev/tanalista-api)"), timeout)
	if err != nil {
		return err
	}
	listener, err := net.Listen("tcp", env("BARCODE_GRPC_ADDR", ":9090"))
	if err != nil {
		return fmt.Errorf("listen: %w", err)
	}
	defer listener.Close()
	server, h := grpcserver.New(barcode.New(provider), 3*time.Second)
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()
	done := make(chan error, 1)
	go func() { done <- server.Serve(listener) }()
	log.Printf("barcode gRPC listening on %s", listener.Addr())
	select {
	case err := <-done:
		if errors.Is(err, grpc.ErrServerStopped) {
			return nil
		}
		return err
	case <-ctx.Done():
		grpcserver.Stop(server, h, 5*time.Second)
		return nil
	}
}

func main() {
	if err := run(); err != nil {
		log.Print(err)
		os.Exit(1)
	}
}
