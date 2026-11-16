# TáNaLista API

Backend do **TáNaLista**, aplicativo colaborativo de listas de compras com Modo Mercado.
A API concentra as regras de negócio, a persistência, o compartilhamento e a
sincronização dos dados usados pelo aplicativo.

## Vídeos

- **Sprint - 0:** [video](https://drive.google.com/file/d/1sXf73865ZDiTYRcJEdk8gAgYPKOgH0KS/view?usp=sharing)
- **Sprint 1:** [vídeo de demonstração](https://youtu.be/arCPkAo0HdQ).

## Disciplinas

- **DIM0547 - Desenvolvimento de Sistemas Web II:** backend, contratos e infraestrutura.
- **DIM0524 - Desenvolvimento de Sistemas para Dispositivos Móveis:** aplicativo cliente.
- **DIM0510 - Processos de Software:** processo de desenvolvimento.

## Equipe

| Nome | Matrícula | GitHub | Papel em DIM0547 |
| --- | --- | --- | --- |
| Vinicius Barbosa | 20230051760 | [vbarbosadev](https://github.com/vbarbosadev) | Desenvolvedor principal e responsável pelo backend |

**Projeto individual em DIM0547:** Vinicius Barbosa é o único integrante e responsável
pelo backend. A equipe do aplicativo mobile pertence ao projeto integrado de DIM0524,
não à equipe desta entrega. As coortes foram substituídas pelas daily meetings.

## Estrutura

```text
api/        Serviço principal em Java 21 com Quarkus
services/   Microsserviços e esqueleto da stack Go
protos/     Contratos Protocol Buffers entre os serviços
docs/       Proposta e documentação do projeto
```

O serviço Quarkus é responsável pelo domínio, persistência, autenticação, autorização e
orquestração dos casos de uso. O primeiro serviço Go, `barcode-lookup-service`, consulta
fontes externas por código de barras e normaliza metadados usando concorrência, timeout e
cancelamento. A comunicação interna usa o contrato gRPC v1 da Sprint 2.

## Execução local

Para rodar o sistema: **Docker com Compose v2**. Não é necessário instalar Java,
Go, Buf ou Maven na máquina; os Dockerfiles geram/compilam dentro dos containers.
O build inicial requer acesso aos registros de imagens e dependências.

Em um clone limpo, na raiz:

```bash
docker compose up --build
```

O Compose sobe PostgreSQL, Go (`127.0.0.1:9090`) e API
(`http://localhost:8081`), com health checks e dependências saudáveis.
Em outro terminal, `docker compose ps` deve indicar serviços saudáveis.
`/q/health/ready` é o health da API; o binário Go usa `-healthcheck`.
O Flyway aplica as
migrações em banco vazio e valida as existentes. O volume preserva os dados em
reinícios; `docker compose down` não remove o volume. Não altere migrações aplicadas.
O esquema vem só do Flyway, nunca de geração Hibernate.

O Compose usa padrões locais de demonstração, portas restritas a loopback e
identidade fixa habilitada explicitamente. **Não é configuração de produção.**
Opcionalmente copie `.env.example` para `.env` e ajuste valores; nunca versione
o arquivo. Senhas padrão não são credenciais de um ambiente público.

Para desenvolvimento fora dos containers, instale
[mise](https://mise.jdx.dev/getting-started.html) e execute `mise install`
(Java 21, Go 1.24, Buf 1.50 e Node 22).

Para usar PostgreSQL já instalado, configure `DB_URL`, `DB_USER` e `DB_PASSWORD`
no `.env` e execute `mise run dev`. Não inicie o banco do Compose na mesma porta.
Para iniciar só o banco: `docker compose up -d postgres`.

O modo de identidade de desenvolvimento **não autentica usuários**. Usa
`DEV_USER_ID` no servidor e só atende ao CRUD quando habilitado; é padrão no
perfil `dev`, mas fora dele exige `DEV_IDENTITY_ENABLED=true`. Use somente em
ambiente confiável; Compose publica portas apenas no loopback. Não habilite em
produção pública. Autenticação real será uma entrega posterior.

Para testes: `mise run test-api` e `mise run test-go`.
Detalhamento das suítes e limites: [docs/testes.md](docs/testes.md).
Java cobre contrato, REST/gRPC controlado e arquitetura; Go cobre domínio,
adaptador HTTP e servidor gRPC. A suíte completa do CRUD com PostgreSQL continua
na tarefa #60; o smoke existente não a substitui.

Para reproduzir build/test/lint e sincronia de stubs: `mise run ci`.
O job Compose adicional é reproduzido com:

```sh
docker compose -f docker-compose.yml -f compose.smoke.yml up -d --build --wait
node scripts/smoke-sprint1.mjs
node scripts/smoke-barcode.mjs
docker compose -f docker-compose.yml -f compose.smoke.yml stop barcode
node scripts/smoke-barcode.mjs --down
node scripts/smoke-sprint1.mjs
```

O override usa fixture HTTP sintética em vez do OFF real; não é deploy.
Não executar simultaneamente com outro Compose nas mesmas portas.
Para encerrar, use `docker compose -f docker-compose.yml -f compose.smoke.yml down`.
O diretório [services/](services/README.md) documenta variáveis, grpcurl e prazos;
[protos/](protos/README.md) documenta geração e erros do contrato.

Swagger UI: <http://localhost:8081/q/swagger-ui/>. OpenAPI gerado do código:
<http://localhost:8081/q/openapi> (JSON com `?format=json`). Inclui os dois CRUDs,
paginação, filtros, UUIDs, respostas de sucesso e erros em Problem Details.

## Fluxo de branches

```text
feature/* ou chore/* -> develop -> main
```

`develop` é a branch padrão de integração. `main` recebe somente versões de entrega ou
produção, sempre por pull request vindo de `develop` e com a CI verde.

## Documentação e processo

- [Proposta do backend](docs/proposta.md)
- [Proposta do aplicativo](docs/tanalista/proposta.md)
- [Usuários e jornadas](docs/jornadas.md)
- [Contrato inicial da API](docs/contrato-api.md)
- [Processo de desenvolvimento](docs/processo.md)
- [Sprint 2: escopo, tarefas, dependências e fluxo de entrega](docs/SPRINT-2.md)
- [Requisitos iniciais](docs/requisitos.md)
- [Domínio: modelo implementado e planejado](docs/dominio.md)
- [Arquitetura do CRUD](docs/arquitetura.md)
- [ADR 0001: serviço Go de consulta por código de barras](docs/decisoes/0001-servico-go-codigo-de-barras.md)
- [Evidências, validação e fechamento da Sprint 1](docs/entrega-sprint1.md)
- [Registro de uso de IA como auxílio](docs/uso-de-ia.md)
- [Backlog no GitHub Projects](https://github.com/users/vbarbosadev/projects/3)
- [Aplicativo mobile](https://github.com/thallystorres/tanalista)

## Status

Sprint 1: CRUD de Lista e Item, PostgreSQL/Flyway, domínio independente,
validação RFC 9457 e OpenAPI publicados em `main` pelo
[PR #53](https://github.com/vbarbosadev/tanalista-api/pull/53), com
[CI verde no commit técnico `cc74acc`](https://github.com/vbarbosadev/tanalista-api/actions/runs/38740126762).
Entrega individual de Vinicius, sem revisão externa. O link do vídeo e a
documentação individual já estão em `main` desde `57b4759`, com CI aprovada.
Acesso anônimo e conteúdo do vídeo ainda precisam de conferência na
[#36](https://github.com/vbarbosadev/tanalista-api/issues/36).

Sprint 2 é a entrega final, em **30/11/2026, 23:59**: Go/gRPC, testes,
Compose reproduzível e API pública segura com banco gerenciado.
O [plano da sprint](docs/SPRINT-2.md) distingue tarefas planejadas de entregas
validadas. Compras e compartilhamento permanecem no backlog do produto.
Consulta Go/gRPC implementada; autenticação pública, deploy e a suíte completa
do CRUD ainda têm tarefas pendentes. A consulta não cria itens automaticamente
e sua indisponibilidade não impede a inclusão manual.
