# Barcode Lookup Service

O `barcode-lookup-service` está planejado para consultar produtos a partir de
códigos EAN/UPC e devolver metadados normalizados ao Quarkus. O código atual
ainda é um bootstrap. A implementação da Sprint 2 usará gRPC, deadlines,
`context` e tratamento explícito de erros, começando por um único provedor:
Open Food Facts. Novas fontes e concorrência adicional dependem de necessidade
justificada, não são requisitos da primeira implementação.

O serviço não persiste catálogo, não cria itens e não acessa as entidades centrais. As
regras de autorização, confirmação e persistência continuam no Quarkus, conforme a
divisão registrada em [`docs/proposta.md`](../docs/proposta.md).

A [ADR 0001](../docs/decisoes/0001-servico-go-codigo-de-barras.md) registra a
justificativa, as alternativas, os custos e as evidências futuras da decisão.
