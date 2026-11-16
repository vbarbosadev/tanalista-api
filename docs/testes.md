# Testes e evidências

## Unitários Java — #59

Executar com Java 21 pelo mise, na raiz:

```sh
mise run test-api
```

Somente os novos unitários, sem inicializar Quarkus ou banco:

```sh
cd api
./mvnw -B -Dtest=DominioTest,CasosDeUsoTest,ItemPatchRequestTest test
```

No Windows, usar `mvnw.cmd` no lugar de `./mvnw`.

| Classe | Garantia |
| --- | --- |
| `DominioTest` | Nome, normalização, identidade, números, opcionais, limites e cópia imutável da página |
| `CasosDeUsoTest` | CRUD com repositórios em memória, unicidade por contexto, filtros, paginação, isolamento entre donos e exclusão |
| `ItemPatchRequestTest` | Unidade do adaptador JSON: ausência preserva valor, null limpa opcional, entradas inválidas são rejeitadas |

Os relógios são fixos: não há espera nem dependência da hora da máquina.
Repositórios de cada teste são novos, sem estado compartilhado entre execuções.
Testar a unidade do PATCH no adaptador não coloca Jackson no domínio.

Esses testes não comprovam SQL, migrações, concorrência PostgreSQL ou transporte
HTTP. O ArchUnit e os testes REST/gRPC existentes continuam rodando; integração
com banco real é descrita abaixo.

## PostgreSQL real — #60

`CrudPostgresTest` é `@QuarkusTest` com perfil isolado e PostgreSQL 16
descartável via Testcontainers. Declara o recurso explicitamente no perfil, sem herdar
o servidor gRPC de outros testes. A dependência Testcontainers segue o BOM Quarkus.

```sh
mise run test-api
# Somente banco:
cd api
./mvnw -B -Dtest=CrudPostgresTest test
```

É necessário Docker em execução, inclusive no Codespaces. A suíte falha se não
houver Docker: não é silenciosamente ignorada, não usa H2 nem aceita URL de um
banco existente. Não há configuração diferente para local e Actions.

São verificados: nome real do banco pelo JDBC, versões Flyway 1/2/3,
CRUDs com relação, PATCH, cascade, filtros/paginação, normalização/unicidade,
visibilidade por dono/lista e conflito de duas escritas concorrentes.
IDs criados são rastreados e removidos após cada teste; o container é descartado.
Não há truncate de banco compartilhado nem limpeza de volume do usuário.

O nome termina em `Test`, portanto roda pelo Surefire no `mvn test`, sem depender
de `skipITs`. O antigo `GreetingResourceIT` é apenas teste de empacotamento e não
é a evidência de integração com banco.
Relatórios Surefire/Failsafe são anexados pelo CI inclusive em falhas.

Nesta máquina Windows sem Docker, os novos testes compilaram e os 12 unitários
foram executados. A integração PostgreSQL deve ser comprovada no CI/Codespaces;
compilação não equivale a execução contra banco.

## Evidência Compose — #62

O [PR #76](https://github.com/vbarbosadev/tanalista-api/pull/76) validou no
Actions: clone limpo sem `.env`, builds Java/Go dentro dos containers,
healthchecks, REST -> Go -> HTTP controlado, CRUD e readiness com Go parado,
migrações, isolamento e persistência após recriar API.

A máquina de desenvolvimento desta etapa não tem Docker instalado. A execução
de Compose acima é evidência do CI, não execução local de containers.
