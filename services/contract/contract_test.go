package contract_test

import (
	"testing"

	barcodev1 "github.com/vbarbosadev/tanalista-api/services/gen/tanalista/barcode/v1"
	"google.golang.org/protobuf/proto"
)

func TestGeneratedContract(t *testing.T) {
	req := &barcodev1.LookupProductRequest{Code: "012345678905"}
	wire, err := proto.Marshal(req)
	if err != nil {
		t.Fatal(err)
	}
	var decoded barcodev1.LookupProductRequest
	if err := proto.Unmarshal(wire, &decoded); err != nil {
		t.Fatal(err)
	}
	if decoded.Code != req.Code {
		t.Fatal("leading zero lost")
	}
	res := &barcodev1.LookupProductResponse{Code: req.Code, Name: "Produto", Source: "open-food-facts"}
	if res.Brand != nil || res.Category != nil || res.Unit != nil {
		t.Fatal("unknown metadata must be absent")
	}
	if barcodev1.BarcodeService_ServiceDesc.ServiceName != "tanalista.barcode.v1.BarcodeService" {
		t.Fatal("unexpected versioned service")
	}
}
