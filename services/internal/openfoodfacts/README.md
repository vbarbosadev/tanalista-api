# Adaptador Open Food Facts

Consulta somente leitura, sem autenticação de provedor:
`GET {OFF_BASE_URL}/api/v2/product/{codigo}.json`.
O endereço pode conter prefixo de caminho (ex.: `/off` numa simulação).

Campos solicitados: `code,product_name,brands,categories,product_quantity_unit`.
`product_name` vira nome; `brands` vira marca; `categories` é um rótulo externo,
não enum do TáNaLista. Unidade normalizada pelo OFF é `g` ou `ml`; qualquer
outra/ausente permanece desconhecida. Não interpretar texto `quantity` por
heurística nem inventar `UNIDADE`.

O domínio preserva o código solicitado, aceita canonicalização com zeros à
esquerda e normaliza espaços. Ausência de produto/nome é `ErrNotFound`;
respostas inválidas, 429, 5xx, redirecionamento e erros de rede são
`ErrUnavailable`. Prazo/cancelamento preservam erros `context`.

O cliente exige timeout positivo e User-Agent identificável. Não segue
redirecionamentos; limita o corpo a 1 MiB, fecha respostas e não repete
requisições automaticamente. Não envia credenciais da API ou do banco.
O `http.Client.Timeout` também limita leitura do corpo; o contexto do chamador
pode impor prazo menor.

## Fontes e limites

- Descoberta Orbit em 16/11/2026: lookup OFF v2, sem chave de leitura,
  User-Agent customizado e seleção de campos.
- [Tutorial oficial](https://openfoodfacts.github.io/openfoodfacts-server/api/tutorial-off-api/).
- [Schema Product Base](https://openfoodfacts.github.io/documentation/docs/Product-Opener/schemas/schemas/product_base/).
- [Termos](https://world.openfoodfacts.org/terms-of-use): dados OFF são sujeitos
  às condições/licenças do provedor, incluindo atribuição. O campo `fonte`
  identifica origem, mas não dispensa conferir os termos antes de publicação.

Não há promessa de cobertura universal nem quota fixa: a documentação consultada
apresenta orientações diferentes por endpoint/ambiente. Tratar 429/503, evitar
consulta por tecla digitada e confirmar condições atuais antes do deploy.
O cliente mobile deve exibir a origem dos dados e permitir revisão manual.

## Testes

```sh
cd services
go test -cover ./internal/openfoodfacts
```

Usam HTTP real em `httptest`, sem rede externa: sucesso, opcionais, envelope
inválido, ausência, 429/5xx/HTML, código divergente, tamanho, cancelamento,
timeout, conexão recusada e redirecionamento.
Há ainda uma [simulação Postman](../../../postman/mocks/open-food-facts/README.md)
opcional que executa o mesmo adaptador contra outro processo.
