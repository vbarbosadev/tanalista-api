# Fixture Open Food Facts v2

Mock local somente leitura para testar o adaptador real, não o sistema sob teste.
O handler é um servidor HTTP Node, carregado diretamente pela simulação.
Não usa estado, corpo de requisição ou autenticação.

Origem: envelope GET `/api/v2/product/{barcode}.json` conferido com Orbit e
[documentação OFF](https://openfoodfacts.github.io/openfoodfacts-server/api/tutorial-off-api/);
campos em [Product Base](https://openfoodfacts.github.io/documentation/docs/Product-Opener/schemas/schemas/product_base/).
Os valores e a associação código/cenário são **sintéticos** e não afirmam que
esses produtos estão presentes/ausentes no provedor real.

A busca por mock do proprietário foi tentada, mas a CLI estava sem login.
Por isso esta fixture foi criada a partir da documentação, não baixada de um
workspace oficial. Não foi publicada no Postman.

| Código | Resposta modelada |
| --- | --- |
| `3017624010701` | 200, envelope de produto utilizável |
| `96385074` | 404, status 0 |
| `012345678905` | 503 JSON |
| `4006381333931` | 500 HTML, como proxy |
| `5901234123457` | 200 após 500 ms; excede o prazo do teste |
| Outros caminhos/códigos | 501, fora do contrato modelado |

## Executar

Na raiz, com Postman CLI >= 1.70:

```sh
postman simulation run postman/simulations/barcode.sim.yaml --port 4900 --output ndjson
```

Em outro terminal, POSIX:

```sh
OFF_SIM_URL=http://127.0.0.1:4900/off mise exec -- go -C services test -count=1 -run TestSimulation ./internal/openfoodfacts
```

PowerShell:

```powershell
$env:OFF_SIM_URL = "http://127.0.0.1:4900/off"
mise exec -- go -C services test -count=1 -run TestSimulation ./internal/openfoodfacts
Remove-Item Env:OFF_SIM_URL
```

Conferir o evento `listening` e cinco eventos `request` no log. A fixture lenta
pode registrar resposta depois que o adaptador já encerrou pelo prazo.
Parar a simulação ao terminar. Não versionar logs.

Sem `OFF_SIM_URL`, esse teste adicional é explicitamente ignorado; os testes
HTTP com `httptest` continuam rodando local e no CI. Nada aqui comprova
disponibilidade, quota ou cobertura do provedor real.
