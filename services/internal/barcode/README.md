# Domínio da consulta

`Service` depende somente da interface `Provider`, sem HTTP, gRPC, SQL ou
framework. `Product` é sugestão, não entidade persistida.

- `ValidateCode`: EAN-8, UPC-A e EAN-13, ASCII e checksum; preserva zeros.
- `Lookup`: valida antes do provedor, propaga context, normaliza espaços,
  exige nome/fonte e mantém campos opcionais desconhecidos vazios.
- Erros nomeados preservados por `%w` e `errors.Is`; cancelamento e deadline
  mantêm os erros padrão de context.
- O provedor deve respeitar o contexto. Um resultado tardio não pode substituir
  cancelamento; o domínio não cria goroutines para esconder um provedor bloqueado.

```sh
cd services
go test -cover ./internal/barcode
```

Os testes usam funções provedoras em memória. Adaptador HTTP e servidor gRPC
são tarefas separadas (#57 e #58).
