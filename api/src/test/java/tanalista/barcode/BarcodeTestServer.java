package tanalista.barcode;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import tanalista.contrato.barcode.v1.*;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** A real gRPC server on an ephemeral loopback port, never a remote dependency. */
public class BarcodeTestServer implements QuarkusTestResourceLifecycleManager {
    private Server server;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public static final AtomicBoolean sawDeadline = new AtomicBoolean();

    @Override
    public Map<String, String> start() {
        try {
            server = ServerBuilder.forPort(0).addService(new BarcodeServiceGrpc.BarcodeServiceImplBase() {
                @Override
                public void lookupProduct(LookupProductRequest request,
                                          StreamObserver<LookupProductResponse> response) {
                    sawDeadline.set(io.grpc.Context.current().getDeadline() != null);
                    switch (request.getCode()) {
                        case "96385074" -> response.onError(Status.NOT_FOUND.withDescription("private provider data").asRuntimeException());
                        case "012345678905" -> response.onError(Status.UNAVAILABLE.withDescription("private provider data").asRuntimeException());
                        case "4006381333931" -> response.onError(Status.INVALID_ARGUMENT.asRuntimeException());
                        case "5901234123457" -> {
                            var future = scheduler.schedule(() -> response.onError(
                                    Status.DEADLINE_EXCEEDED.asRuntimeException()), 2, TimeUnit.SECONDS);
                            io.grpc.Context.current().addListener(ctx -> future.cancel(false), Runnable::run);
                        }
                        default -> {
                            response.onNext(LookupProductResponse.newBuilder().setCode(request.getCode())
                                    .setName("Produto").setSource("open-food-facts").build());
                            response.onCompleted();
                        }
                    }
                }
            }).build().start();
            return Map.of(
                    "quarkus.grpc.clients.barcode.host", "localhost",
                    "quarkus.grpc.clients.barcode.port", Integer.toString(server.getPort()),
                    "quarkus.grpc.clients.barcode.test-port", Integer.toString(server.getPort()),
                    "tanalista.barcode.timeout-ms", "1000",
                    "tanalista.dev-identity.enabled", "true");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stop() {
        if (server != null) server.shutdownNow();
        scheduler.shutdownNow();
    }
}
