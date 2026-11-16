package tanalista.adaptadores.grpc;

import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tanalista.contrato.barcode.v1.BarcodeServiceGrpc;
import tanalista.contrato.barcode.v1.LookupProductRequest;
import tanalista.dominio.ConsultaCodigoBarras;
import tanalista.dominio.ErroConsultaCodigoBarras;
import tanalista.dominio.MetadadosProduto;

import java.util.concurrent.TimeUnit;

import static tanalista.dominio.ErroConsultaCodigoBarras.Tipo.*;

@ApplicationScoped
public class ConsultaBarcodeGrpc implements ConsultaCodigoBarras {
    private final BarcodeServiceGrpc.BarcodeServiceBlockingStub stub;
    private final long timeoutMs;

    public ConsultaBarcodeGrpc(
            @GrpcClient("barcode") BarcodeServiceGrpc.BarcodeServiceBlockingStub stub,
            @ConfigProperty(name = "tanalista.barcode.timeout-ms", defaultValue = "2500") long timeoutMs) {
        if (timeoutMs < 1 || timeoutMs > 10000) {
            throw new IllegalArgumentException("Barcode timeout must be between 1 and 10000ms");
        }
        this.stub = stub;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public MetadadosProduto consultar(String codigo) {
        if (codigo == null || !codigo.matches("[0-9]{8}|[0-9]{12}|[0-9]{13}")) {
            throw new ErroConsultaCodigoBarras(INVALIDO);
        }
        try {
            var resultado = stub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                    .lookupProduct(LookupProductRequest.newBuilder().setCode(codigo).build());
            if (!codigo.equals(resultado.getCode()) || resultado.getName().isBlank()
                    || resultado.getSource().isBlank()) {
                throw new ErroConsultaCodigoBarras(INTERNO);
            }
            return new MetadadosProduto(resultado.getCode(), resultado.getName(),
                    resultado.hasBrand() ? resultado.getBrand() : null,
                    resultado.hasCategory() ? resultado.getCategory() : null,
                    resultado.hasUnit() ? resultado.getUnit() : null, resultado.getSource());
        } catch (StatusRuntimeException erro) {
            throw new ErroConsultaCodigoBarras(switch (erro.getStatus().getCode()) {
                case INVALID_ARGUMENT -> INVALIDO;
                case NOT_FOUND -> AUSENTE;
                case UNAVAILABLE, CANCELLED, RESOURCE_EXHAUSTED -> INDISPONIVEL;
                case DEADLINE_EXCEEDED -> PRAZO_EXCEDIDO;
                default -> INTERNO;
            });
        }
    }
}
