package tanalista.contrato;

import org.junit.jupiter.api.Test;
import tanalista.contrato.barcode.v1.BarcodeServiceGrpc;
import tanalista.contrato.barcode.v1.LookupProductRequest;
import tanalista.contrato.barcode.v1.LookupProductResponse;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeContractTest {
    @Test
    void generatedContractPreservesCodeAndOptionalPresence() throws Exception {
        var request = LookupProductRequest.newBuilder().setCode("012345678905").build();
        assertEquals("012345678905", LookupProductRequest.parseFrom(request.toByteArray()).getCode());
        var response = LookupProductResponse.newBuilder()
                .setCode(request.getCode()).setName("Produto").setSource("open-food-facts").build();
        assertFalse(response.hasBrand());
        assertFalse(response.hasCategory());
        assertFalse(response.hasUnit());
        assertEquals("tanalista.barcode.v1.BarcodeService",
                BarcodeServiceGrpc.getServiceDescriptor().getName());
        assertEquals(6, LookupProductResponse.SOURCE_FIELD_NUMBER);
    }
}
