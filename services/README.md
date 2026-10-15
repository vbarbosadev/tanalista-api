# Barcode Lookup Service

O `barcode-lookup-service` consulta fontes externas a partir de códigos EAN/UPC e
devolve metadados normalizados ao serviço Quarkus. A implementação da Sprint 2 usará
gRPC, deadlines, `context`, concorrência entre provedores e tratamento explícito de
erros.

O serviço não persiste catálogo, não cria itens e não acessa as entidades centrais. As
regras de autorização, confirmação e persistência continuam no Quarkus, conforme a
divisão registrada em [`docs/proposta.md`](../docs/proposta.md).
