# TáNaLista API

Backend do **TáNaLista**, aplicativo colaborativo de listas de compras com Modo Mercado.
A API concentra as regras de negócio, a persistência, o compartilhamento e a
sincronização dos dados usados pelo aplicativo.

## Videos
- **Sprint - 0:** [video](https://drive.google.com/file/d/1sXf73865ZDiTYRcJEdk8gAgYPKOgH0KS/view?usp=sharing)

## Disciplinas

- **DIM0547 - Desenvolvimento de Sistemas Web II:** backend, contratos e infraestrutura.
- **DIM0524 - Desenvolvimento de Sistemas para Dispositivos Móveis:** aplicativo cliente.
- **DIM0510 - Processos de Software:** processo de desenvolvimento.

## Equipe

| Nome | Matrícula | GitHub | Papel em DIM0547 |
| --- | --- | --- | --- |
| Vinicius Barbosa | 20230051760 | [vbarbosadev](https://github.com/vbarbosadev) | Desenvolvedor principal e responsável pelo backend |
| Thallys | 20240011552 | [thallystorres](https://github.com/thallystorres) | Apoio na validação do domínio e critérios de aceitação |
| Ivis | 20220028454 | [ivixs](https://github.com/ivixs) | Apoio na organização do backlog e processo |

**Coorte:** B - apresentação online.

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
cancelamento. A comunicação interna será feita por gRPC a partir da Sprint 2.

## Execução local

Pré-requisitos: [mise](https://mise.jdx.dev/getting-started.html) e Docker Compose
(ou PostgreSQL 16+ instalado). O mise instala Java 21 e Go.

1. Copie `.env.example` para `.env` e ajuste as credenciais locais. Não versione
   `.env`. O mise carrega esse arquivo para as tarefas; o Compose também o lê.
2. Execute:

```bash
mise install
mise run build-api
mise run up
```

O Compose sobe PostgreSQL e a API em `http://localhost:8081`. O Flyway aplica as
migrações em banco vazio e valida as existentes. O volume preserva os dados em
reinícios; `mise run down` não remove o volume. Não altere migrações aplicadas.
O esquema vem só do Flyway, nunca de geração Hibernate.

Para usar PostgreSQL já instalado, configure `DB_URL`, `DB_USER` e `DB_PASSWORD`
no `.env` e execute `mise run dev`. Não inicie o banco do Compose na mesma porta.
Para iniciar só o banco: `docker compose up -d postgres`.

O modo de identidade de desenvolvimento **não autentica usuários**. Usa
`DEV_USER_ID` no servidor e só atende ao CRUD quando habilitado; é padrão no
perfil `dev`, mas fora dele exige `DEV_IDENTITY_ENABLED=true`. Use somente em
ambiente confiável; Compose publica portas apenas no loopback. Não habilite em
produção pública. Autenticação real será uma entrega posterior.

Para reproduzir a CI: `mise run ci` (Java e Go; testes existentes preservados).

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
- [Requisitos iniciais](docs/requisitos.md)
- [Domínio: modelo implementado e planejado](docs/dominio.md)
- [Backlog no GitHub Projects](https://github.com/users/vbarbosadev/projects/3)
- [Aplicativo mobile](https://github.com/thallystorres/tanalista)
- Vídeo da Sprint 0: aguardando publicação

## Status

Sprint 0: preparação do monorepo, proposta, backlog e integração contínua.
