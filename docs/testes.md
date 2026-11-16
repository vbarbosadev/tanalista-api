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
com banco real é acompanhada separadamente em #60.

## Evidência Compose — #62

O [PR #76](https://github.com/vbarbosadev/tanalista-api/pull/76) validou no
Actions: clone limpo sem `.env`, builds Java/Go dentro dos containers,
healthchecks, REST -> Go -> HTTP controlado, CRUD e readiness com Go parado,
migrações, isolamento e persistência após recriar API.

A máquina de desenvolvimento desta etapa não tem Docker instalado. A execução
de Compose acima é evidência do CI, não execução local de containers.
