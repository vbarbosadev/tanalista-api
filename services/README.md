# Barcode Lookup Service

O `barcode-lookup-service` valida EAN-8, UPC-A e EAN-13, consulta o Open Food
Facts e expõe sugestões por gRPC. Domínio, HTTP externo e transporte são
adaptadores separados, com deadline, cancelamento e erros explícitos.
Novas fontes e concorrência adicional dependem de necessidade justificada.

O serviço não persiste catálogo, não cria itens e não acessa as entidades centrais. As
regras de autorização, confirmação e persistência continuam no Quarkus, conforme a
divisão registrada em [`docs/proposta.md`](../docs/proposta.md).

A [ADR 0001](../docs/decisoes/0001-servico-go-codigo-de-barras.md) registra a
justificativa, as alternativas, os custos e as evidências futuras da decisão.

## Executar e testar

Na raiz, após `mise install`:

```sh
mise exec -- go -C services run ./cmd/barcode-lookup
mise run test-go
mise exec -- go run github.com/fullstorydev/grpcurl/cmd/grpcurl@v1.9.3 -plaintext localhost:9090 list
```

O processo imprime o endereço em que escuta. O RPC é
`tanalista.barcode.v1.BarcodeService/LookupProduct`, com JSON
`{"code":"3017624010701"}` e deadline no cliente. A consulta real acessa o
provedor; para testar sem rede externa, usar a
[simulação local](../postman/mocks/open-food-facts/README.md).

| Variável | Padrão | Finalidade |
| --- | --- | --- |
| `BARCODE_GRPC_ADDR` | `:9090` | Endereço de escuta interno |
| `BARCODE_HEALTH_ADDR` | `127.0.0.1:9090` | Destino do comando `-healthcheck`; ajustar junto da porta |
| `OFF_BASE_URL` | `https://world.openfoodfacts.org` | Provedor ou simulação (`http://127.0.0.1:4900/off`) |
| `OFF_USER_AGENT` | `TaNaLista/1.0 (https://github.com/vbarbosadev/tanalista-api)` | Identificação e contato |
| `OFF_TIMEOUT` | `2s` | Limite HTTP, sujeito a prazo menor do chamador |

O servidor limita RPCs unários a 3 segundos. Health (`SERVING`) e reflection
estão registrados; saúde é do processo, não promessa de disponibilidade do OFF.
O binário aceita `-healthcheck` e encerra com código de saída correspondente.

SIGTERM/Ctrl+C iniciam encerramento gracioso, limitado a 5 segundos.
O Go não acessa banco. Transporte local é plaintext e deve ficar em rede
confiável; não expor gRPC sem proteção diretamente à internet.

Testes `bufconn` passam pela pilha gRPC e cobrem erros, opcionais, prazo,
health, reflection e encerramento limitado. A API REST e o Compose são
integrações acompanhadas separadamente em #27 e #62.
