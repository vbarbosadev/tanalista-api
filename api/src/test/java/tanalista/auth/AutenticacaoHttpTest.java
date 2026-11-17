package tanalista.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/** Real HTTP and RSA verification, without mock security identities or real credentials. */
@QuarkusTest
@TestProfile(AuthProfile.class)
class AutenticacaoHttpTest {
    private static final String ROOT = "/api/v1/listas";
    @Inject ObjectMapper json;
    @ConfigProperty(name = "tanalista.test.jwt.private-key")
    String privada;

    @BeforeAll
    static void protegerLogs() {
        RestAssured.config = RestAssured.config.logConfig(
                LogConfig.logConfig().blacklistHeader("Authorization"));
    }

    private Map<String, Object> claims(String subject) {
        var now = Instant.now().getEpochSecond();
        return new HashMap<>(Map.of("iss", "https://issuer.example/", "sub", subject,
                "aud", "tanalista-api", "iat", now, "exp", now + 300));
    }

    private String token(Map<String, Object> claims) throws Exception {
        return token(claims, "RS256");
    }

    private String token(Map<String, Object> claims, String alg) throws Exception {
        var encoder = Base64.getUrlEncoder().withoutPadding();
        var header = encoder.encodeToString(json.writeValueAsBytes(Map.of("alg", alg, "typ", "JWT")));
        var payload = encoder.encodeToString(json.writeValueAsBytes(claims));
        var mensagem = header + "." + payload;
        var chave = KeyFactory.getInstance("RSA").generatePrivate(
                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privada)));
        var assinatura = Signature.getInstance("SHA256withRSA");
        assinatura.initSign(chave);
        assinatura.update(mensagem.getBytes(StandardCharsets.US_ASCII));
        return mensagem + "." + encoder.encodeToString(assinatura.sign());
    }

    private void rejeitado(String authorization) {
        given().header("Authorization", authorization).get(ROOT).then().statusCode(401)
                .header("WWW-Authenticate", containsString("Bearer"))
                .contentType("application/problem+json").body("status", equalTo(401))
                .body("detail", equalTo("Credencial Bearer ausente ou inválida."));
    }

    @Test
    void semTokenOuTokenMalformadoNaoAcessaNenhumaRotaDeNegocio() {
        for (var path : new String[]{ROOT, ROOT + "/" + UUID.randomUUID() + "/itens",
                "/api/v1/codigos-barras/3017624010701"}) {
            given().get(path).then().statusCode(401).header("WWW-Authenticate", containsString("Bearer"));
        }
        given().contentType("application/json").body("{\"nome\":\"Nova\"}").post(ROOT)
                .then().statusCode(401);
        rejeitado("Bearer not-a-jwt");
        rejeitado("Basic abc");
        rejeitado("Bearer ");
    }

    @Test
    void validaAssinaturaAlgoritmoEmissorAudienciaPrazoEClaimsObrigatorias() throws Exception {
        var valido = token(claims("alice"));
        var parts = valido.split("\\.");
        rejeitado("Bearer " + parts[0] + "." + parts[1] + ".AAAA");
        rejeitado("Bearer " + token(claims("alice"), "none"));
        rejeitado("Bearer " + token(claims("alice"), "HS256"));
        for (var campo : new String[]{"iss", "aud", "sub", "iat", "exp"}) {
            var semClaim = claims("alice");
            semClaim.remove(campo);
            rejeitado("Bearer " + token(semClaim));
        }
        for (var alteracao : Map.<String, Object>of(
                "iss", "https://other.example/", "aud", "other-api",
                "sub", " ", "exp", Instant.now().minusSeconds(300).getEpochSecond(),
                "nbf", Instant.now().plusSeconds(300).getEpochSecond()).entrySet()) {
            var invalido = claims("alice");
            invalido.put(alteracao.getKey(), alteracao.getValue());
            rejeitado("Bearer " + token(invalido));
        }
    }

    @Test
    void crudAutenticadoPreservaIsolamentoEntreUsuarios() throws Exception {
        var alice = token(claims("provider|alice"));
        var bob = token(claims("provider|bob"));
        var id = given().auth().oauth2(alice).contentType("application/json")
                .body(Map.of("nome", "Auth " + UUID.randomUUID())).post(ROOT)
                .then().statusCode(201).extract().<String>path("id");
        var lista = ROOT + "/" + id;
        try {
            var item = given().auth().oauth2(alice).contentType("application/json")
                    .body(Map.of("nome", "Café")).post(lista + "/itens")
                    .then().statusCode(201).extract().<String>path("id");
            given().auth().oauth2(token(claims("provider|alice"))).get(lista)
                    .then().statusCode(200).body("id", equalTo(id));
            var variasAudiencias = claims("provider|alice");
            variasAudiencias.put("aud", List.of("other-api", "tanalista-api"));
            given().auth().oauth2(token(variasAudiencias)).get(lista).then().statusCode(200);
            given().auth().oauth2(bob).get(lista).then().statusCode(404);
            given().auth().oauth2(bob).get(lista + "/itens").then().statusCode(404);
            given().auth().oauth2(bob).get(lista + "/itens/" + item).then().statusCode(404);
            given().auth().oauth2(bob).contentType("application/json").body("{\"nome\":\"Intruso\"}")
                    .post(lista + "/itens").then().statusCode(404);
            given().auth().oauth2(bob).contentType("application/json").body("{\"nome\":\"Invadido\"}")
                    .patch(lista + "/itens/" + item).then().statusCode(404);
            given().auth().oauth2(bob).delete(lista + "/itens/" + item).then().statusCode(404);
            given().auth().oauth2(bob).get(ROOT).then().statusCode(200).body("total", equalTo(0));
            given().auth().oauth2(bob).contentType("application/json").body("{\"nome\":\"Invadida\"}")
                    .patch(lista).then().statusCode(404);
            given().auth().oauth2(bob).delete(lista).then().statusCode(404);
            given().auth().oauth2(alice).contentType("application/json").body("{\"nome\":\"Atualizada\"}")
                    .patch(lista).then().statusCode(200).body("nome", equalTo("Atualizada"));
            given().auth().oauth2(alice).delete(lista + "/itens/" + item).then().statusCode(204);
        } finally {
            given().auth().oauth2(alice).delete(lista).then().statusCode(204);
        }
    }

    @Test
    void healthEContratoContinuamAcessiveisSemCredencial() {
        given().get("/q/health/ready").then().statusCode(200);
        given().queryParam("format", "json").get("/q/openapi").then().statusCode(200)
                .body("components.securitySchemes.bearerAuth.scheme", equalTo("bearer"))
                .body("paths.'/api/v1/listas'.get.responses.'401'", notNullValue());
    }
}
