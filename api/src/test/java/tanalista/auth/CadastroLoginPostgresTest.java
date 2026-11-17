package tanalista.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import jakarta.inject.Inject;
import io.agroal.api.AgroalDataSource;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(LocalAuthProfile.class)
@Timeout(60)
class CadastroLoginPostgresTest {
    private static final String ROOT = "/api/v1/auth";
    private static final String SENHA = "Somente-Teste-123!";
    @Inject AgroalDataSource datasource;

    @BeforeAll static void logs() {
        // Never print credential request bodies on assertion failures.
        RestAssured.config = RestAssured.config.logConfig(
                LogConfig.logConfig().blacklistHeader("Authorization"));
    }

    private String email() { return UUID.randomUUID() + "@example.test"; }
    private Map<String, String> dados(String email) { return Map.of("email", email, "senha", SENHA); }
    private String cadastrar(String email) {
        return given().contentType("application/json").body(dados(email)).post(ROOT + "/cadastro")
                .then().statusCode(201).header("Cache-Control", "no-store")
                .body("senha", nullValue()).body("senhaHash", nullValue()).extract().path("id");
    }
    private String login(String email) {
        return given().contentType("application/json").body(dados(email)).post(ROOT + "/login")
                .then().statusCode(200).header("Cache-Control", "no-store")
                .body("token_type", equalTo("Bearer")).body("expires_in", equalTo(1800))
                .extract().path("access_token");
    }

    @Test void cadastroLoginMeCrudEIsolamentoComPostgresReal() throws Exception {
        String a = email(), b = email();
        var id = cadastrar(a);
        cadastrar(b);
        var token = login(a);
        var outro = login(b);
        given().auth().oauth2(token).get(ROOT + "/me").then().statusCode(200)
                .body("id", equalTo(id)).body("email", equalTo(a));
        given().get(ROOT + "/me").then().statusCode(401);
        given().auth().oauth2(token).get("/q/health/ready").then().statusCode(200);
        var lista = given().auth().oauth2(token).contentType("application/json")
                .body(Map.of("nome", "Cadastro " + UUID.randomUUID())).post("/api/v1/listas")
                .then().statusCode(201).body("donoId", equalTo(id)).extract().<String>path("id");
        String path = "/api/v1/listas/" + lista;
        try {
            var item = given().auth().oauth2(token).contentType("application/json")
                    .body(Map.of("nome", "Arroz")).post(path + "/itens")
                    .then().statusCode(201).extract().<String>path("id");
            given().auth().oauth2(outro).get(path).then().statusCode(404);
            given().auth().oauth2(outro).get(path + "/itens/" + item).then().statusCode(404);
            given().auth().oauth2(outro).delete(path).then().statusCode(404);
            given().auth().oauth2(login(a)).get(path).then().statusCode(200);
            given().auth().oauth2(token).get("/api/v1/codigos-barras/3017624010701")
                    .then().statusCode(503);
        } finally {
            given().auth().oauth2(token).delete(path).then().statusCode(204);
        }
        try (var c = datasource.getConnection();
             var s = c.prepareStatement("SELECT senha_hash FROM usuario WHERE id = ?")) {
            s.setObject(1, UUID.fromString(id));
            try (var r = s.executeQuery()) {
                assertTrue(r.next());
                assertTrue(r.getString(1).startsWith("$argon2id$"));
                assertNotEquals(SENHA, r.getString(1));
            }
        }
    }

    @Test void validaEntradasDuplicidadeConcorrenteELoginGenerico() {
        String email = email();
        cadastrar(email);
        given().contentType("application/json").body(dados(" " + email.toUpperCase() + " "))
                .post(ROOT + "/cadastro").then().statusCode(409).contentType("application/problem+json");
        for (var entrada : new String[]{"email-invalido", "x@localhost"}) {
            given().contentType("application/json").body(dados(entrada))
                    .post(ROOT + "/cadastro").then().statusCode(422);
        }
        given().contentType("application/json").body(Map.of("email", email(), "senha", "curta"))
                .post(ROOT + "/cadastro").then().statusCode(422);
        for (var entrada : new String[]{email, email(), "invalido"}) {
            given().contentType("application/json").body(Map.of("email", entrada, "senha", "Senha-Errada-123"))
                    .post(ROOT + "/login").then().statusCode(401)
                    .body("detail", equalTo("E-mail ou senha inválidos."));
        }
        String concorrente = email();
        var a = CompletableFuture.supplyAsync(() -> given().contentType("application/json")
                .body(dados(concorrente)).post(ROOT + "/cadastro").statusCode());
        var b = CompletableFuture.supplyAsync(() -> given().contentType("application/json")
                .body(dados(concorrente.toUpperCase())).post(ROOT + "/cadastro").statusCode());
        assertEquals(java.util.List.of(201, 409),
                java.util.stream.Stream.of(a.join(), b.join()).sorted().toList());
    }
}
