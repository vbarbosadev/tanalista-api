# TáNaLista API

Backend do **TáNaLista**, aplicativo colaborativo de listas de compras com Modo Mercado.
A API concentra as regras de negócio, a persistência, o compartilhamento e a
sincronização dos dados usados pelo aplicativo.

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
orquestração dos casos de uso. A responsabilidade específica do primeiro serviço Go será
definida antes da Sprint 2 e deverá justificar o uso de concorrência, I/O intensivo,
processamento em lote ou cache.

## Pré-requisitos

- [mise](https://mise.jdx.dev/getting-started.html)
- PostgreSQL 16 instalado e em execução na máquina

O `mise` instala as versões de Java e Go declaradas em `mise.toml`.

O ambiente de desenvolvimento usa estas configurações locais:

| Configuração | Valor |
| --- | --- |
| Host | `localhost` |
| Porta | `5432` |
| Banco | `tanalista` |
| Usuário | `tanalista` |
| Senha de desenvolvimento | `tanalista_pass` |

Essas credenciais são exclusivas do ambiente local e não devem ser reutilizadas em
produção.

## Execução local

```bash
mise install
mise run build
mise run test
mise run dev
```

A API usa `http://localhost:8081`. Ao iniciar, o Flyway aplica as migrações no PostgreSQL
local disponível em `localhost:5432`.

Para reproduzir as verificações do GitHub Actions:

```bash
mise run ci
```

O `docker-compose.yml` é mantido como alternativa reproduzível para quem não possui
PostgreSQL instalado. Nesse caso, use:

```bash
mise run up
mise run down
```

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
- [Backlog no GitHub Projects](https://github.com/users/vbarbosadev/projects/3)
- [Aplicativo mobile](https://github.com/thallystorres/tanalista)
- Vídeo da Sprint 0: aguardando publicação

## Status

Sprint 0: preparação do monorepo, proposta, backlog e integração contínua.
