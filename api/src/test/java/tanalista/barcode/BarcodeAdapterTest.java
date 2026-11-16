package tanalista.barcode;

import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import tanalista.adaptadores.grpc.ConsultaBarcodeGrpc;
import tanalista.contrato.barcode.v1.*;
import tanalista.dominio.ErroConsultaCodigoBarras;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static tanalista.dominio.ErroConsultaCodigoBarras.Tipo.*;

class BarcodeAdapterTest {
    @Test
    void unavailableServerIsBoundedAndDoesNotPreventClientConstruction() throws Exception {
        var server = ServerBuilder.forPort(0).build().start();
        int port = server.getPort();
        server.shutdownNow().awaitTermination(2, TimeUnit.SECONDS);
        var channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build();
        try {
            var adapter = new ConsultaBarcodeGrpc(BarcodeServiceGrpc.newBlockingStub(channel), 200);
            long start = System.nanoTime();
            var error = assertThrows(ErroConsultaCodigoBarras.class, () -> adapter.consultar("3017624010701"));
            assertTrue(error.tipo() == INDISPONIVEL || error.tipo() == PRAZO_EXCEDIDO);
            assertTrue(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(2));
        } finally {
            channel.shutdownNow();
        }
    }

    @Test
    void malformedSuccessAndUnexpectedErrorsAreNotExposed() throws Exception {
        var server = ServerBuilder.forPort(0).addService(new BarcodeServiceGrpc.BarcodeServiceImplBase() {
            @Override
            public void lookupProduct(LookupProductRequest request, StreamObserver<LookupProductResponse> response) {
                if (request.getCode().equals("96385074")) {
                    response.onError(Status.INTERNAL.withDescription("secret").asRuntimeException());
                } else {
                    response.onNext(LookupProductResponse.newBuilder().setName("Produto").build());
                    response.onCompleted();
                }
            }
        }).build().start();
        var channel = ManagedChannelBuilder.forAddress("localhost", server.getPort()).usePlaintext().build();
        try {
            var adapter = new ConsultaBarcodeGrpc(BarcodeServiceGrpc.newBlockingStub(channel), 1000);
            for (var code : new String[]{"3017624010701", "96385074"}) {
                var error = assertThrows(ErroConsultaCodigoBarras.class, () -> adapter.consultar(code));
                assertEquals(INTERNO, error.tipo());
                assertFalse(error.getMessage().contains("secret"));
            }
        } finally {
            channel.shutdownNow();
            server.shutdownNow();
        }
    }
}
