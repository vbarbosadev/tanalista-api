# Roteiro individual do vídeo — Sprint 1 (5 minutos)

**Apresentador: Vinicius. Vídeo ainda não gravado/publicado (#36).** A entrega
está sendo concluída individualmente, conforme informado pelo responsável.
Não declarar participação ou revisão de outras pessoas. A regra acadêmica de
participação em grupo continua na rubrica: comunicar a situação ao professor,
sem presumir dispensa ou garantia de nota.

## Antes de gravar

1. Atualize `main` após a release e anote seu hash: `git rev-parse HEAD`.
2. Configure `.env` a partir de `.env.example`, sem mostrá-lo na gravação.
3. Com Docker: `mise run build-api` e `mise run up`.
4. Sem Docker: configure o PostgreSQL local e execute `mise run dev`.
   `DEV_IDENTITY_ENABLED=true` é apenas para ambiente confiável.
5. Em outro terminal, execute `mise run smoke` (Node 22+).
6. Abra Swagger em `http://localhost:8081/q/swagger-ui/`, o Actions verde de
   `main`, `docker-compose.yml`, migrações e os pacotes do código.
7. Use nome de lista novo em cada ensaio. Crie e exclua apenas dados seus.

Sem Docker local, mostre o Compose e o job real `Compose, migrations and CRUD
smoke` no Actions. Diga que a execução reproduzível ocorreu no CI; não finja
que iniciou containers na máquina. Para evidenciar persistência local, use
Ctrl+C no terminal da API e `mise run dev` novamente, sem parar o PostgreSQL.

## 0:00–0:30 — Apresentação

**Tela:** README e Swagger.

> Olá, sou Vinicius e estou concluindo esta entrega individualmente. O TáNaLista
> é uma API para planejamento de listas de compras. Nesta sprint implementei
> CRUD completo de Lista e Item, relacionados de um para muitos, com PostgreSQL,
> migrações Flyway, separação de camadas, validação e OpenAPI gerado do código.
> A identidade usada aqui é temporária de desenvolvimento, não autenticação real.

## 0:30–2:00 — Dois CRUDs, paginação e filtros

**Tela:** Swagger; ensaie as operações para evitar procurar botões.

1. POST `/api/v1/listas` com `{"nome":"Vídeo Sprint 1"}`. Copie `listaId`.
2. Mostre **201**, `Location` e GET `/listas/{listaId}`.
3. Crie outra lista `{"nome":"Vídeo Sprint 1 — Reserva"}`. GET `/listas`
   com `nome=video sprint 1`, `size=1`: mostre total e próxima página.
4. PATCH da primeira lista com `{"nome":"Vídeo Sprint 1 — Mercado"}`.
5. POST `/listas/{listaId}/itens` com:

   ```json
   {
     "nome": "Café",
     "quantidade": 2,
     "unidade": "UNIDADE",
     "categoria": "ALIMENTOS",
     "precoEstimado": 6.50
   }
   ```

6. Copie `itemId`. Crie outro item com `{"nome":"Leite 200g","categoria":"ALIMENTOS"}`.
7. GET `/listas/{listaId}/itens` com `size=1`; depois filtre `nome=leite` e
   `categoria=ALIMENTOS`. Consulte o Café pelo `itemId` na rota individual.
8. PATCH do Café com `{"quantidade":3}`; mostre os outros campos preservados.
9. DELETE do Leite e da lista Reserva: **204 sem corpo**. Mantenha a lista
   Mercado e o Café para demonstrar persistência no próximo bloco.

**Fala enquanto executa:**

> A lista tem UUID e a criação retorna 201 com Location. Os itens pertencem a uma
> lista por chave estrangeira e aparecem em rotas aninhadas. As consultas têm
> paginação, filtros e total; a paginação é executada no SQL com tamanho máximo
> de cem, sem carregar tudo em memória. A atualização usa PATCH e a remoção
> retorna 204. Aqui estou demonstrando as cinco operações nas duas entidades.

## 2:00–3:00 — Banco, migrações e persistência

**Tela:** Compose, arquivos de migração e terminal; depois Swagger.

> O Compose configura PostgreSQL e API com dependência de saúde do banco e volume
> para os dados. As credenciais vêm do ambiente. O Flyway é a única origem do
> esquema: a geração automática do Hibernate está desabilitada. A V1 legada foi
> preservada; a V2 cria Lista e Item com chave estrangeira; a V3 garante a
> unicidade do nome normalizado de item por lista, inclusive em concorrência.

- Mostre `api/src/main/resources/db/migration/` e
  `quarkus.hibernate-orm.schema-management.strategy=none`.
- Com Docker: `docker compose restart api`. Sem Docker: reinicie só a API local
  e mostre o job Compose verde no Actions como evidência dos containers.
- Consulte novamente a lista Mercado e o Café pelos mesmos UUIDs.

> Após reiniciar a API, os mesmos dados permanecem no PostgreSQL. O Flyway valida
> as migrações existentes sem recriar o esquema.

## 3:00–4:00 — Arquitetura

**Tela:** árvore de pacotes e imports dos arquivos abaixo.

| Arquivo/pacote | O que destacar |
| --- | --- |
| `dominio/Lista.java` e `dominio/Item.java` | Entidades imutáveis e invariantes, sem JPA/Quarkus. |
| `dominio/ListaRepository.java` e `dominio/ItemRepository.java` | Portas de persistência. |
| `aplicacao/Listas.java` e `aplicacao/Itens.java` | Casos de uso dependem do domínio, sem framework. |
| `adaptadores/banco` | Entidades JPA separadas e implementação PostgreSQL/Panache. |
| `adaptadores/http` | Recursos, DTOs, transações e tradução de erros. |
| `adaptadores/memoria` | Alternativa para compor testes de domínio na próxima sprint. |

> As dependências apontam para o domínio, que importa apenas a biblioteca padrão
> Java. Os casos de uso recebem interfaces e não conhecem Hibernate ou HTTP.
> O banco traduz entidades JPA para os modelos do domínio. O código anterior de
> Produto foi mantido como legado e não faz parte do CRUD apresentado.

## 4:00–4:35 — Validação e Problem Details

**Tela:** Swagger e respostas HTTP.

1. POST Lista com `{"nome":""}` → **422**.
2. POST Item `{"nome":"  CAFE  "}` na lista que contém Café → **409**.
3. GET Lista com `listaId=invalido` → **400**.
4. GET Lista com `listaId=ffffffff-ffff-ffff-ffff-ffffffffffff` → **404**,
   desde que esse UUID não exista no seu banco.

> Entradas são validadas antes da persistência. Caixa, acentos e espaços não
> permitem itens equivalentes na mesma lista. Os erros são traduzidos em um
> ponto central para application/problem+json, com type, title, status, detail
> e instance, conforme RFC 9457, sem expor SQL ou credenciais.

## 4:35–5:00 — CI e encerramento

**Tela:** Actions da branch `main`, hash e issues concluídas.

> O OpenAPI é gerado do código e a Swagger UI está disponível na aplicação
> empacotada. A CI compila Java e Go, mantém o teste existente e executa um smoke
> com PostgreSQL e Compose para CRUD, migrações, isolamento e persistência.
> Testes de domínio, integração e arquitetura ficam para a Sprint 2, assim como
> compras e gRPC. Os merges foram autorizados para esta entrega individual,
> sem declarar revisão externa. Este é o hash técnico apresentado.

## Após gravar — publicação obrigatória

- [ ] Duração próxima de cinco minutos, áudio claro e código/respostas legíveis.
- [ ] YouTube não listado ou Drive com acesso para qualquer pessoa com o link.
- [ ] Link testado em janela anônima e publicado na seção Vídeos do README.
- [ ] Atualizar o estado deste roteiro para vídeo publicado.
- [ ] Commit e PR da publicação para `develop`, CI verde e merge.
- [ ] Nova release `develop` → `main` para incluir o link na entrega avaliada.
- [ ] Fechar #36 somente depois de o link real estar em `main`.
- [ ] Registrar hash final de `main`, CI e link na #35 e no canal da disciplina.

O roteiro não equivale ao vídeo. Publicação e avaliação diferenciada por entrega
individual ainda dependem, respectivamente, do responsável e do professor.
