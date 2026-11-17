package tanalista.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/** Full signing/login/verification HTTP path with ephemeral keys, no Docker needed. */
@QuarkusTest
@TestProfile(LocalAuthMemoryProfile.class)
class LocalAuthHttpTest {
    @Test void emissaoLocalValidaPeloMesmoVerificadorEPermiteCrud() {
        String email = UUID.randomUUID() + "@example.test";
        var dados = Map.of("email", email, "senha", "Ficticia-para-teste-123");
        var id = given().contentType("application/json").body(dados).post("/api/v1/auth/cadastro")
                .then().statusCode(201).extract().<String>path("id");
        var token = given().contentType("application/json").body(dados).post("/api/v1/auth/login")
                .then().statusCode(200).header("Cache-Control", "no-store").body("expires_in", equalTo(1800))
                .extract().<String>path("access_token");
        given().auth().oauth2(token).get("/api/v1/auth/me").then().statusCode(200)
                .body("id", equalTo(id)).body("email", equalTo(email));
        var lista = given().auth().oauth2(token).contentType("application/json").body(Map.of("nome", "Minha"))
                .post("/api/v1/listas").then().statusCode(201).body("donoId", equalTo(id))
                .extract().<String>path("id");
        given().auth().oauth2(token).delete("/api/v1/listas/" + lista).then().statusCode(204);
        given().get("/api/v1/auth/me").then().statusCode(401);
        given().contentType("application/json").body(Map.of("email", email, "senha", "Outra-senha-123"))
                .post("/api/v1/auth/login").then().statusCode(401)
                .body("detail", equalTo("E-mail ou senha inválidos."));
    }

    @Test void limiteHttpNaoPodeSerContornadoComCabecalhoForwarded() {
        String email = UUID.randomUUID() + "@example.test";
        for (int i = 0; i < 10; i++) {
            given().header("X-Forwarded-For", "192.0.2." + i).contentType("application/json")
                    .body(Map.of("email", email, "senha", "Incorreta-123456"))
                    .post("/api/v1/auth/login").then().statusCode(401);
        }
        given().header("X-Forwarded-For", "198.51.100.1").contentType("application/json")
                .body(Map.of("email", email, "senha", "Incorreta-123456"))
                .post("/api/v1/auth/login").then().statusCode(429)
                .header("Retry-After", "60").contentType("application/problem+json");
    }
}
