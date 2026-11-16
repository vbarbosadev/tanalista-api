FROM bufbuild/buf:1.50.0 AS buf-tools
FROM golang:1.24-alpine AS build
COPY --from=buf-tools /usr/local/bin/buf /usr/local/bin/buf
WORKDIR /workspace/services
COPY services/go.mod services/go.sum ./
RUN go mod download
COPY services/ ./
COPY protos/ /workspace/protos/
WORKDIR /workspace/protos
RUN buf lint && buf generate
WORKDIR /workspace/services
RUN CGO_ENABLED=0 go build -trimpath -ldflags="-s -w" -o /out/barcode-lookup ./cmd/barcode-lookup

FROM alpine:3.22
RUN apk add --no-cache ca-certificates
COPY --from=build /out/barcode-lookup /usr/local/bin/barcode-lookup
USER 10001
EXPOSE 9090
HEALTHCHECK --interval=5s --timeout=2s --retries=10 CMD ["/usr/local/bin/barcode-lookup", "-healthcheck"]
ENTRYPOINT ["/usr/local/bin/barcode-lookup"]
