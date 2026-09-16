# Contrato inicial da API

Contrato de planejamento entre o aplicativo TáNaLista e a API Java/Quarkus. O OpenAPI
implementado deverá manter compatibilidade com estas operações ou registrar a mudança.

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
