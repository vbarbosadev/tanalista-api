# Contrato inicial da API

Contrato entre o aplicativo TáNaLista e a API Java/Quarkus. Na Sprint 1 estão
implementados somente os recursos **Lista e Item** abaixo. OpenAPI é gerado do
código em `/q/openapi`; Swagger UI em `/q/swagger-ui/`, inclusive no JAR empacotado.
Usuários, acessos, compras e código de barras permanecem planejamento futuro.

## Recorte implementado na Sprint 1

| Método | Caminho (prefixo `/api/v1`) | Sucesso |
| --- | --- | --- |
| POST | `/listas` | 201 + Location |
| GET | `/listas` | 200, página |
| GET | `/listas/{listaId}` | 200, lista |
| PATCH | `/listas/{listaId}` | 200, lista renomeada |
| DELETE | `/listas/{listaId}` | 204, sem corpo |
| POST | `/listas/{listaId}/itens` | 201 + Location |
| GET | `/listas/{listaId}/itens` | 200, página |
| GET | `/listas/{listaId}/itens/{itemId}` | 200, item |
| PATCH | `/listas/{listaId}/itens/{itemId}` | 200, item alterado |
| DELETE | `/listas/{listaId}/itens/{itemId}` | 204, sem corpo |

- Identidade fixa configurada no servidor, sem receber dono por parâmetro ou
  cabeçalho. `DEV_IDENTITY_ENABLED` precisa estar habilitado; caso contrário, 503.
  Isto **não é autenticação**. Listas de outro dono e seus itens retornam 404.
- `page=0`, `size=20`, máximo 100; offset máximo 2147483647; paginação no SQL.
  Resposta: `items`, `page`, `size`, `total`. Página além do fim retorna `items: []`.
  Parâmetros repetidos/desconhecidos e page/size vazios retornam 400;
  categoria vazia retorna 422.
- Listas: filtro `nome` (substring normalizada, até 120 caracteres), ordem
  `atualizadaEm DESC, id ASC`. Itens: filtros `nome` e `categoria` (igualdade
  sensível a caixa, até 60 caracteres), ordem `nomeNormalizado ASC, id ASC`.
- Nome: 1 a 120 caracteres não vazios; unicidade normalizada por dono/lista.
  Quantidade opcional, positiva, até 9 dígitos inteiros e 3 decimais; preço
  estimado opcional, não negativo, até 9 inteiros e 2 decimais. Unidade opcional
  até 30 caracteres; categoria opcional até 60; strings vazias são inválidas.
- PATCH de item distingue ausência (mantém) de `null` (limpa opcional). Nome
  não pode ser nulo. `{}` é inválido. Campos desconhecidos e tipos JSON errados
  são rejeitados. Escritas usam transação; itens atualizam a data da lista.
- Lista retorna `id`, `donoId`, `nome`, `criadaEm`, `atualizadaEm`, sem progresso
  de compras ainda não implementadas. Item retorna seus campos e `listaId`.
- Exclusão de lista remove seus itens planejados. Preservação de histórico de
  compras será implementada junto de Compra/RegistroCompra; não há histórico agora.
- Erros: 400 para JSON/UUID/parâmetro/tipo malformado, 422 para validação e limites,
  404 para inexistente/não visível, 409 para duplicidade, 405/406/415 para protocolo,
  500 genérico sem dados sensíveis e 503 para identidade desabilitada. Formato RFC
  9457: `type`, `title`, `status`, `detail`, `instance`, `application/problem+json`.
  A implementação usa `type: "about:blank"`; URIs específicas abaixo são planejadas.

## Planejamento do produto além do recorte

As seções seguintes preservam o contrato inicial. Autenticação, papéis, progresso,
idempotência e controle de versão ainda não estão implementados na Sprint 1.

## Convenções

- Base path: `/api/v1`.
- Conteúdo: `application/json`.
- Datas: ISO 8601 em UTC.
- Identificadores: UUID.
- Paginação: `page`, `size` e resposta com `items`, `page`, `size` e `total`.
- Autenticação futura: `Authorization: Bearer <token>`.
- Escritas sincronizadas podem enviar `Idempotency-Key`.
- Erros seguem `application/problem+json`, conforme RFC 9457.

## Recursos

| Método | Caminho | Resultado | Sucesso |
| --- | --- | --- | --- |
| `POST` | `/usuarios` | Cria uma conta | `201` |
| `GET` | `/listas` | Lista as listas acessíveis ao usuário | `200` |
| `POST` | `/listas` | Cria uma lista e atribui o criador como dono | `201` |
| `GET` | `/listas/{listaId}` | Consulta lista, itens e progresso | `200` |
| `PATCH` | `/listas/{listaId}` | Renomeia uma lista | `200` |
| `DELETE` | `/listas/{listaId}` | Exclui uma lista como dono | `204` |
| `POST` | `/listas/{listaId}/acessos` | Concede acesso de editor | `201` |
| `DELETE` | `/listas/{listaId}/acessos/{usuarioId}` | Revoga acesso de editor | `204` |
| `GET` | `/listas/{listaId}/itens` | Lista os itens planejados | `200` |
| `POST` | `/listas/{listaId}/itens` | Adiciona um item | `201` |
| `PATCH` | `/listas/{listaId}/itens/{itemId}` | Altera um item | `200` |
| `DELETE` | `/listas/{listaId}/itens/{itemId}` | Remove um item planejado | `204` |
| `POST` | `/listas/{listaId}/compras` | Inicia uma Compra | `201` |
| `GET` | `/compras/{compraId}` | Consulta a Compra e seu total | `200` |
| `PUT` | `/compras/{compraId}/registros/{itemId}` | Marca ou atualiza um item comprado | `200` |
| `DELETE` | `/compras/{compraId}/registros/{itemId}` | Desmarca um item | `204` |
| `PATCH` | `/compras/{compraId}` | Finaliza a Compra | `200` |
| `GET` | `/codigos-barras/{codigo}` | Consulta metadados externos de um produto | `200` |

## Estruturas principais

### Criar lista

```json
{
  "nome": "Compra do mês"
}
```

```json
{
  "id": "8f9305ad-d2aa-4ce8-bf1c-69378e9324bf",
  "nome": "Compra do mês",
  "papel": "DONO",
  "quantidadeItens": 0,
  "atualizadaEm": "2026-09-16T12:00:00Z"
}
```

### Adicionar item

```json
{
  "nome": "Leite 1L",
  "quantidade": 2,
  "unidade": "UNIDADE",
  "categoria": "ALIMENTOS",
  "precoEstimado": 6.50
}
```

### Registrar item comprado

```json
{
  "precoEncontrado": 6.29,
  "quantidadeLevada": 2
}
```

A resposta inclui `subtotal` e o `total` atualizado da Compra.

### Consultar código de barras

```json
{
  "codigo": "7891000100103",
  "nome": "Leite integral",
  "marca": "Exemplo",
  "categoria": "ALIMENTOS",
  "unidade": "UNIDADE",
  "fonte": "open-food-facts"
}
```

A consulta não cria um Item automaticamente. O aplicativo permite revisar os metadados
e envia a criação do Item ao Quarkus pela operação de lista correspondente.

## Contrato interno com Go

O Quarkus chama o `barcode-lookup-service` por gRPC usando uma operação unária:

```text
LookupProduct(BarcodeRequest) returns (ProductMetadata)
```

`BarcodeRequest` contém o código EAN/UPC. `ProductMetadata` contém código, nome, marca,
categoria, unidade e fonte. A chamada sempre possui deadline. O Go diferencia código
inválido, produto não encontrado, indisponibilidade do provedor e deadline excedido.

## Erros

```json
{
  "type": "https://tanalista.app/problems/nome-duplicado",
  "title": "Nome já utilizado",
  "status": 409,
  "detail": "Já existe um item equivalente nesta lista.",
  "instance": "/api/v1/listas/8f9305ad/itens"
}
```

| Status | Uso |
| --- | --- |
| `400` | JSON inválido ou parâmetro malformado |
| `401` | Credencial ausente ou inválida |
| `403` | Usuário autenticado sem permissão sobre o recurso |
| `404` | Recurso não visível ou código sem produto encontrado nas fontes externas |
| `409` | Nome normalizado duplicado, Compra já aberta ou conflito de versão |
| `503` | Todos os provedores externos de código de barras estão indisponíveis |
| `422` | Entrada sintaticamente válida que viola uma regra de domínio |

## Idempotência e concorrência

Operações originadas na fila offline usam `Idempotency-Key`. A mesma chave, usuário e
operação retornam a resposta original sem repetir efeitos. Recursos mutáveis expõem uma
versão; uma escrita baseada em versão antiga resulta em `409 Conflict`. O fechamento da
Compra é transacional e impede novos registros após o estado `FINALIZADA`.
