package tanalista.banco;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(PostgresProfile.class)
class CrudPostgresTest {
    private static final String ROOT = "/api/v1/listas";
    @Inject AgroalDataSource datasource;
    private final List<UUID> listasCriadas = new ArrayList<>();

    private UUID criarLista(String nome) {
        var id = UUID.fromString(given().contentType("application/json")
                .body(Map.of("nome", nome)).post(ROOT).then().statusCode(201)
                .header("Location", containsString(ROOT + "/")).extract().path("id"));
        listasCriadas.add(id);
        return id;
    }

    private String itens(UUID lista) {
        return ROOT + "/" + lista + "/itens";
    }

    @AfterEach
    void limparSomenteDadosDesteTeste() throws SQLException {
        // IDs are created by this test only. This is not truncate or a shared database.
        try (var connection = datasource.getConnection();
             var statement = connection.prepareStatement("DELETE FROM lista WHERE id = ?")) {
            for (var id : listasCriadas) {
                statement.setObject(1, id);
                statement.executeUpdate();
            }
        }
    }

    @Test
    void flywayAplicaMigracoesReaisSemGeracaoDoOrm() throws Exception {
        try (var connection = datasource.getConnection()) {
            assertTrue(connection.getMetaData().getDatabaseProductName().contains("PostgreSQL"));
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery(
                         "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank")) {
                var versoes = new ArrayList<String>();
                while (rows.next()) versoes.add(rows.getString(1));
                assertEquals(List.of("1", "2", "3"), versoes);
            }
        }
        given().get("/q/health/ready").then().statusCode(200);
    }

    @Test
    void crudDeListaEItemRelacionadosComPatchECascade() throws Exception {
        var lista = criarLista("Mercado " + UUID.randomUUID());
        var path = ROOT + "/" + lista;
        var item = given().contentType("application/json").body(Map.of(
                "nome", "Café", "quantidade", 2, "unidade", "UN",
                "categoria", "Alimentos", "precoEstimado", 6.50))
                .post(itens(lista)).then().statusCode(201).extract().<String>path("id");
        given().get(itens(lista) + "/" + item).then().statusCode(200)
                .body("listaId", equalTo(lista.toString())).body("nome", equalTo("Café"));
        var quantidade = given().contentType("application/json").body("{\"quantidade\":3,\"categoria\":null}")
                .patch(itens(lista) + "/" + item).then().statusCode(200)
                .body("nome", equalTo("Café"))
                .body("categoria", nullValue()).body("unidade", equalTo("UN"))
                .extract().jsonPath().getDouble("quantidade");
        assertEquals(3.0, quantidade);
        given().contentType("application/json").body(Map.of("nome", "Renomeada " + UUID.randomUUID()))
                .patch(path).then().statusCode(200).body("id", equalTo(lista.toString()));
        given().delete(itens(lista) + "/" + item).then().statusCode(204).body(is(""));
        given().get(itens(lista) + "/" + item).then().statusCode(404);
        var outro = given().contentType("application/json").body(Map.of("nome", "Arroz"))
                .post(itens(lista)).then().statusCode(201).extract().<String>path("id");
        given().delete(path).then().statusCode(204).body(is(""));
        given().get(path).then().statusCode(404);
        try (var c = datasource.getConnection();
             var s = c.prepareStatement("SELECT count(*) FROM item WHERE id = ?")) {
            s.setObject(1, UUID.fromString(outro));
            try (var rows = s.executeQuery()) {
                rows.next();
                assertEquals(0, rows.getInt(1));
            }
        }
    }

    @Test
    void filtrosPaginacaoEUnicidadeSaoExecutadosNoPostgres() {
        var prefixo = "Teste " + UUID.randomUUID();
        var lista = criarLista(prefixo + " Café");
        criarLista(prefixo + " Reserva");
        given().queryParam("nome", prefixo.toLowerCase()).queryParam("size", 1)
                .get(ROOT).then().statusCode(200).body("total", equalTo(2)).body("items.size()", equalTo(1));
        given().queryParam("nome", prefixo).queryParam("page", 1).queryParam("size", 1)
                .get(ROOT).then().statusCode(200).body("total", equalTo(2)).body("items.size()", equalTo(1));
        given().contentType("application/json").body(Map.of("nome", prefixo + " CAFE"))
                .post(ROOT).then().statusCode(409).contentType("application/problem+json");
        for (var nome : List.of("Café", "Leite", "Arroz")) {
            given().contentType("application/json").body(Map.of("nome", nome, "categoria", "Alimentos"))
                    .post(itens(lista)).then().statusCode(201);
        }
        given().queryParam("size", 1).queryParam("page", 1).get(itens(lista))
                .then().statusCode(200).body("total", equalTo(3)).body("items[0].nome", equalTo("Café"));
        given().queryParam("nome", "cafe").queryParam("categoria", "Alimentos").get(itens(lista))
                .then().statusCode(200).body("total", equalTo(1)).body("items[0].nome", equalTo("Café"));
        given().queryParam("page", 50).get(itens(lista)).then().statusCode(200)
                .body("total", equalTo(3)).body("items", empty());
        given().contentType("application/json").body(Map.of("nome", " CAFE "))
                .post(itens(lista)).then().statusCode(409);
        given().contentType("application/json").body("{\"nome\":\"Inválido\",\"quantidade\":0}")
                .post(itens(lista)).then().statusCode(422);
        given().queryParam("size", 101).get(itens(lista)).then().statusCode(422);
    }

    @Test
    void outroDonoEOutraListaNaoExpoemItens() throws Exception {
        var alheia = UUID.randomUUID();
        var itemAlheio = UUID.randomUUID();
        listasCriadas.add(alheia);
        try (var c = datasource.getConnection();
             var s = c.prepareStatement("""
                     INSERT INTO lista(id,dono_id,nome,nome_normalizado,criada_em,atualizada_em)
                     VALUES (?,?,'Privada','privada',now(),now())
                     """)) {
            s.setObject(1, alheia);
            s.setObject(2, UUID.randomUUID());
            s.executeUpdate();
            try (var item = c.prepareStatement("INSERT INTO item(id,lista_id,nome,nome_normalizado) VALUES (?,?,'Segredo','segredo')")) {
                item.setObject(1, itemAlheio);
                item.setObject(2, alheia);
                item.executeUpdate();
            }
        }
        var path = ROOT + "/" + alheia;
        given().get(path).then().statusCode(404);
        given().delete(path).then().statusCode(404);
        given().contentType("application/json").body("{\"nome\":\"Invadida\"}")
                .patch(path).then().statusCode(404);
        given().get(itens(alheia)).then().statusCode(404);
        given().get(itens(alheia) + "/" + itemAlheio).then().statusCode(404);
        given().contentType("application/json").body("{\"nome\":\"Novo\"}")
                .post(itens(alheia)).then().statusCode(404);
        var propria = criarLista("Própria " + UUID.randomUUID());
        given().get(itens(propria) + "/" + itemAlheio).then().statusCode(404);
        given().queryParam("nome", "privada").get(ROOT).then().statusCode(200).body("total", equalTo(0));
    }

    @Test
    void concorrenciaPreservaRestricaoETraduzConflito() {
        var lista = criarLista("Concorrência " + UUID.randomUUID());
        var a = CompletableFuture.supplyAsync(() -> given().contentType("application/json")
                .body("{\"nome\":\"Café\"}").post(itens(lista)).statusCode());
        var b = CompletableFuture.supplyAsync(() -> given().contentType("application/json")
                .body("{\"nome\":\"CAFE\"}").post(itens(lista)).statusCode());
        var resultados = new ArrayList<>(List.of(a.join(), b.join()));
        Collections.sort(resultados);
        assertEquals(List.of(201, 409), resultados);
        given().get(itens(lista)).then().statusCode(200).body("total", equalTo(1));
    }
}
