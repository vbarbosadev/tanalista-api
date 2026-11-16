# ADR 0001 — Serviço Go de consulta por código de barras

- **Data:** 16/11/2026.
- **Status:** aceita como decisão de arquitetura; implementação pendente.
- **Responsável:** Vinicius Barbosa (`@vbarbosadev`).
- **Issue:** [#56](https://github.com/vbarbosadev/tanalista-api/issues/56).
- **História:** [#23](https://github.com/vbarbosadev/tanalista-api/issues/23).

## Contexto

O TáNaLista permite planejar compras com listas e itens. A consulta por código
de barras reduz a digitação ao sugerir metadados de um produto, mas não pode
se tornar condição para criar um item manualmente ou usar os demais CRUDs.
Os dados externos podem estar incompletos, indisponíveis ou não conter o produto.

O trabalho predominante dessa consulta é esperar uma fonte HTTP externa,
validar o código e traduzir sua resposta para um modelo estável do produto.
Latência, indisponibilidade e cancelamento têm características diferentes das
operações transacionais de Lista e Item no PostgreSQL.

A Sprint 2 exige um serviço Go com responsabilidade justificada e comunicação
gRPC. Essa exigência, sozinha, não justifica dividir um CRUD: os serviços não
devem compartilhar tabelas nem coordenar uma transação para criar um Item.
O contrato inicial já prevê consulta de metadados sem persistência automática.

## Decisão

**Isolar a consulta externa e a normalização de metadados por código de barras
no `barcode-lookup-service`, em Go, chamado exclusivamente pela API Quarkus
por gRPC com deadline.**

### Responsabilidades e limites

| Componente | Responsabilidade |
| --- | --- |
| Cliente | Enviar código, apresentar sugestões e permitir revisão ou digitação manual |
| API Quarkus | Identidade, autorização, REST, orquestração, tradução de erros e persistência |
| Serviço Go | Validar EAN/UPC, consultar fonte externa e normalizar metadados com origem |
| PostgreSQL | Dados de domínio mantidos exclusivamente pela API |

O Go não acessa as tabelas centrais, não recebe credenciais do banco, não mantém
catálogo persistente, não resolve autorização de usuários e não cria nem altera
Item. O cliente nunca precisa conhecer o endereço gRPC ou o formato do provedor.

Fluxo-alvo:

```text
Cliente -> REST Quarkus -> gRPC com deadline -> Go -> HTTPS provedor
Cliente <- metadados ou erro explícito <--------+

Usuário revisa os dados ou digita manualmente
Cliente -> REST de criação de Item -> Quarkus -> PostgreSQL
```

Os dois fluxos são separados: consultar não causa escrita. A confirmação e as
regras de criação permanecem na operação de Item, inclusive quando não houver
sugestões externas.

### Provedor inicial e concorrência

Usar **Open Food Facts como único provedor inicial**, conforme o planejamento
já registrado. Sua cobertura não será tratada como catálogo completo de todos
os produtos: ausência de resultado é um comportamento esperado.

O adaptador HTTP fica atrás de uma interface pequena definida pelo consumidor.
Validação e modelo de metadados ficam em Go puro, sem imports de HTTP, gRPC ou
banco. O adaptador entrega dados normalizados; tipos específicos do provedor
não atravessam o contrato entre serviços.

Não adicionar goroutines, novas fontes ou cache apenas para demonstrar
concorrência. Com uma única consulta por requisição, o valor imediato de Go
está na implementação de I/O cancelável, isolamento do componente e empacotamento
em binário próprio. Caso novas fontes sejam justificadas, concorrência limitada
e política de escolha de resultado exigirão critérios e testes próprios.

Endpoints, campos efetivamente disponíveis, limites de uso, identificação do
cliente e obrigações de atribuição/licença serão conferidos na tarefa do
provedor (#57), antes da implementação. Esta ADR não afirma ter validado a API
externa nem concede direitos sobre seus dados.

### Prazos, falhas e isolamento

- Toda chamada Quarkus -> Go terá deadline configurável; nenhuma espera ilimitada.
- Operações Go que podem esperar recebem `context.Context` como primeiro
  parâmetro e propagam cancelamento ao adaptador HTTP.
- O orçamento do HTTP respeita o prazo restante da chamada gRPC, sem trabalho
  em background após o término da requisição.
- Erros de domínio serão nomeados e distinguíveis com `errors.Is`; adaptadores
  traduzem código inválido, produto ausente, indisponibilidade e prazo excedido.
- Detalhes de status gRPC, mensagens Protobuf e tradução REST serão definidos
  em #26 e #27, sem acoplar domínio a esses protocolos.
- Falha do Go produz erro explícito da consulta, não produto inventado, sucesso
  vazio ou falha generalizada da API.
- Listas, itens e inclusão manual continuam funcionando sem o Go. A saúde da
  API não será condicionada exclusivamente à disponibilidade desse serviço.

Essas propriedades são objetivos verificáveis, não evidências de que o código
atual já as atende. Um processo separado não limita falhas sozinho: isolamento
depende de deadlines, cancelamento e ausência de dependência do CRUD.

## Alternativas consideradas

### Consulta dentro do Quarkus

É tecnicamente viável: Java/Quarkus também permite I/O concorrente, timeout e
cancelamento. Evitaria um processo, contrato interno, serialização e salto de
rede; seria a alternativa mais simples para uma única fonte em um produto pequeno.

Entretanto, o cliente do provedor e sua evolução seriam empacotados e
implantados junto da API, compartilhando seu processo e orçamento de recursos.
Seria necessário manter os mesmos cuidados de prazo e isolamento dentro dela.
Para esta sprint, preferimos explicitar a fronteira da integração externa e
aprender a operá-la/testá-la como serviço, atendendo também à exigência acadêmica.
Não adotamos Go por suposta incapacidade de Java de realizar esse trabalho.

### Consulta direta pelo aplicativo

Eliminaria a chamada interna, mas acoplaria o cliente ao provedor, à normalização
e às políticas de falha. Novas fontes ou mudanças de formato exigiriam atualizar
o aplicativo. Rejeitada para manter um contrato REST estável no Quarkus.

### Dividir o CRUD ou criar um catálogo Go com banco compartilhado

Não resolve a necessidade de consulta externa e introduz acoplamento de esquema,
autorização e transações. Rejeitada: Go não é um segundo dono das tabelas da API.

## Consequências

### Benefícios

- Fronteira explícita e testável entre regras transacionais e integração externa.
- Troca/evolução do adaptador externo sem levar seus formatos ao cliente.
- Processo Go com build e imagem próprios, sem dependência do PostgreSQL.
- Falhas de consulta podem ser limitadas no tempo sem impedir o fluxo manual.

### Custos e riscos

- Mais um processo, configuração de rede, geração de stubs e operação no Compose.
- Mais um salto de rede e cenários de erro do que na alternativa interna.
- Contrato precisa evoluir de modo compatível; dois builds precisam concordar.
- Ganho de escala ou desempenho não está comprovado e não é premissa da decisão.
- Sem cache inicial, cada consulta pode consumir capacidade do provedor; prazo,
  limites e eventual necessidade de cache serão avaliados, não presumidos.

O Go será avaliado junto da API no Compose. Sua publicação na internet é
opcional na rubrica; a plataforma e a estratégia de implantação serão tratadas
em outra ADR (#65). Não tornar a publicação do Go requisito para o CRUD público.

## Como verificar a decisão

| Evidência futura | Tarefa |
| --- | --- |
| Contrato versionado, comentado, lint e stubs reproduzíveis | #26 |
| Domínio sem infraestrutura, erros e testes de cancelamento/prazo | #28 |
| Adaptador externo testado com HTTP controlado, sem depender da rede | #57 |
| Servidor gRPC com tradução de erros, health e encerramento gracioso | #58 |
| Cliente Quarkus com deadline e teste contra servidor gRPC controlado | #27 |
| API e CRUD operantes com Go interrompido; Compose do zero | #27 e #62 |

Fechar #56 comprova a decisão documentada, não a implementação dessas tarefas.
Reavaliar a separação se o custo operacional superar seu benefício após a
sprint, ou se a responsabilidade passar a exigir dados/transações centrais.

## Referências

- [Contrato planejado da consulta](../contrato-api.md#contrato-interno-com-go).
- [Proposta e divisão Java/Go](../proposta.md#6-divisão-entre-o-serviço-principal-e-go).
- [Plano de execução da Sprint 2](../SPRINT-2.md).
- [Guia oficial — microsserviço Go](https://github.com/fmarquesfilho/web2-2026-2/blob/main/docs/SPRINT-2.md#microsserviço-go).
