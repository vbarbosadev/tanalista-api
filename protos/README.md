# Contratos Protocol Buffers

Fonte única: `tanalista/barcode/v1/barcode.proto`.
Operação: `BarcodeService.LookupProduct(LookupProductRequest)` retorna
`LookupProductResponse`. Os nomes concretos atendem ao Buf STANDARD, substituindo
os nomes ilustrativos `BarcodeRequest`/`ProductMetadata` do planejamento.

```sh
mise install
mise run lint-proto
mise run generate-proto
mise run check-generated
mise run build-api
mise run test-go
```

O Buf e os plugins Go têm versões fixas. `buf generate` usa plugins locais via
`go run`, sem depender do BSR nem de geradores instalados globalmente.
Stubs Go em `services/gen/` são versionados e nunca editados à mão.
Java gera em `api/target/generated-sources/grpc` com `quarkus-grpc`, lendo este
mesmo diretório no build; não copiar `.proto` para a API.

## Semântica v1

- Código string, sem trim/conversão numérica: EAN-8, UPC-A ou EAN-13 com
  dígito verificador. UPC-E e GTIN-14 não são suportados.
- Código, nome e fonte são obrigatórios em sucesso. Nome ausente não é sucesso.
- Marca, categoria e unidade são opcionais com presença explícita; não inventar
  valores. Categoria externa não é um enum do aplicativo; unidade desconhecida
  não é automaticamente `UNIDADE`.
- Consulta é somente leitura, sem criar Item nem consultar tabelas da API.
- Toda chamada deve ter deadline; cliente e servidor propagam cancelamento.

| Estado | gRPC | REST planejado |
| --- | --- | --- |
| Código inválido | `INVALID_ARGUMENT` | 422 |
| Sem produto utilizável | `NOT_FOUND` | 404 |
| Provedor ou Go inacessível | `UNAVAILABLE` | 503 |
| Prazo excedido | `DEADLINE_EXCEEDED` | 504 |
| Cliente cancelou | `CANCELLED` | Requisição interrompida, sem sucesso artificial |
| Falha inesperada | `INTERNAL` | 500 genérico, sem detalhes internos |

Não repetir chamadas automaticamente na v1. O usuário pode tentar novamente ou
digitar o Item manualmente; CRUDs permanecem disponíveis sem Go.
Adicionar campos com novos números; nunca reutilizar números removidos
(reservá-los). Mudanças incompatíveis exigem outro pacote/versionamento.

Os testes dos dois stacks comprovam compilação e semântica das mensagens
geradas. O fluxo real será validado nas tarefas #58 e #27.
