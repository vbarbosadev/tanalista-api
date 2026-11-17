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

## Autenticação JWT genérica — #64

`ConfiguracaoAutenticacaoTest` testa guardas de configuração e incompatibilidade
com a identidade fixa; `IdentificadorExternoTest` testa estabilidade e separação
de UUID por issuer/subject. São JUnit puros.

`AutenticacaoHttpTest` inicia Quarkus com auth habilitada, chaves RSA geradas
temporariamente e repositórios em memória selecionados somente nesse perfil.
Testa HTTP real, assinatura/algoritmo, issuer/aud, claims obrigatórias,
expiração/nbf, ausência de credencial, desafio Bearer, Problem Details e CRUD
com isolamento entre dois usuários. O perfil não herda o servidor gRPC global
nem consulta banco externo. Cabeçalho Authorization é ocultado nos logs de teste.

Não substitui PostgreSQL/Testcontainers nem comprova login, JWKS remoto,
rotação ou integração com fornecedor. Ver [autenticacao.md](autenticacao.md)
para o contrato, configuração e validação ainda necessária antes do deploy.

## Cadastro e login locais — #64

`UsuariosTest` cobre hash/salt Argon2id, normalização, login e falhas genéricas.
`LimiteAutenticacaoTest` usa relógio controlado para limites por conta/IP,
expiração da janela e concorrência de hash.
`LocalAuthHttpTest` cobre cadastro, emissão/verificação JWT, perfil, CRUD e
429 com cabeçalhos de proxy arbitrários, com repositórios em memória.

`CadastroLoginPostgresTest` usa PostgreSQL descartável e RSA efêmero para
cadastro concorrente (201/409), persistência de hash, login, perfil e CRUD
isolado entre usuários. O recurso tem lifecycle próprio, sem Neon real.
A suíte existente confere Flyway V1 a V4. Os testes em memória não substituem
essa integração. ArchUnit também proíbe Bouncy Castle no domínio/aplicação.
Comandos e limites operacionais: [autenticacao-local.md](autenticacao-local.md).
