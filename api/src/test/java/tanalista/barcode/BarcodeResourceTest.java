package tanalista.barcode;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(BarcodeTestServer.class)
class BarcodeResourceTest {
    @Test
    void restCallsRealGrpcWithDeadlineAndOptionalFields() {
        given().when().get("/api/v1/codigos-barras/3017624010701")
                .then().statusCode(200)
                .body("codigo", equalTo("3017624010701"))
                .body("nome", equalTo("Produto"))
                .body("fonte", equalTo("open-food-facts"))
                .body("marca", nullValue());
        assertTrue(BarcodeTestServer.sawDeadline.get());
    }

    @Test
    void errorsAreProblemDetailsWithoutPrivateData() {
        for (var sample : new String[][]{
                {"bad", "422"}, {"96385074", "404"}, {"012345678905", "503"},
                {"4006381333931", "422"}, {"5901234123457", "504"}}) {
            given().when().get("/api/v1/codigos-barras/" + sample[0])
                    .then().statusCode(Integer.parseInt(sample[1]))
                    .contentType("application/problem+json")
                    .body("status", equalTo(Integer.parseInt(sample[1])))
                    .body("detail", not(containsString("private provider data")));
        }
        given().when().get("/hello").then().statusCode(200);
        given().when().get("/q/health/ready").then().statusCode(200);
    }

    @Test
    void openApiIncludesEndpointAndErrors() {
        given().queryParam("format", "json").when().get("/q/openapi")
                .then().statusCode(200)
                .body("paths.'/api/v1/codigos-barras/{codigo}'.get.responses.'504'", notNullValue())
                .body("paths.'/api/v1/codigos-barras/{codigo}'.get.responses.'503'", notNullValue());
    }
}
