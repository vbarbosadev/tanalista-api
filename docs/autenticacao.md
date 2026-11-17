# Autenticação da API por Bearer JWT

Este documento descreve o modo **externo**, com `LOCAL_AUTH_ENABLED=false`.
Por decisão posterior do responsável, o modo próprio com cadastro e login
também está disponível: [autenticacao-local.md](autenticacao-local.md) e ADR 0004.
Nesse modo a chave pública é um arquivo PEM e o subject é o UUID do usuário.
Não confundir os requisitos ou migrar dados automaticamente entre os modos.

## Recorte

A API é um resource server: valida access tokens JWT RS256 emitidos por
um provedor compatível com OIDC. Não cria contas, senhas, sessões, login,
refresh tokens nem implementa descoberta OIDC ou introspecção de tokens opacos.
Não há provedor contratado/configurado nesta etapa nem deploy público validado.

O contrato fonte é o OpenAPI gerado pelas anotações Java. As rotas de listas,
itens e consulta de código de barras exigem Bearer quando `AUTH_ENABLED=true`.
Swagger possui o esquema `bearerAuth` e botão **Authorize**. Health e OpenAPI
continuam acessíveis sem token; documentação aberta não torna o CRUD público.

## Configurar um provedor aprovado

Criar/registrar uma API no provedor escolhido, com audiência própria da API,
distinta do client ID do mobile. O aplicativo deve obter **access tokens** para
essa API; ID tokens de login não são credenciais da API.
Não inferir URLs de chaves: obter issuer e JWKS na documentação/metadados do
provedor, fora da entrada enviada por usuários.

Na plataforma de execução ou no `.env` ignorado pelo Git:

```dotenv
AUTH_ENABLED=true
DEV_IDENTITY_ENABLED=false
OIDC_ISSUER=https://SEU_EMISSOR/
OIDC_JWKS_URL=https://SEU_EMISSOR/CAMINHO_JWKS
OIDC_AUDIENCE=IDENTIFICADOR_DA_API
```

Substituir placeholders, preservando o issuer exato, inclusive barra final.
Issuer, audiência e JWKS são configuração; tokens, senha Neon e demais
credenciais pertencem aos segredos da plataforma. Nunca colocar uma chave
privada do provedor na API: ela precisa somente das chaves públicas JWKS.

Com autenticação habilitada, o startup falha se também houver identidade
fixa, configuração incompleta ou issuer/JWKS não HTTPS. Arquivo de chave local
é permitido somente no modo de teste automatizado, com chaves efêmeras.
Não desabilitar validação TLS para resolver configuração do provedor.

`mise run dev` lê `.env` na raiz. O Compose continua exclusivamente local,
com identidade fixa e banco local; não o usar como manifesto de produção.

## Validação e autorização

SmallRye JWT verifica assinatura RS256, issuer, audiência, expiração e `nbf`,
quando presente. `iss`, `sub`, `aud`, `iat` e `exp` são obrigatórios.
A tolerância de relógio é 30 segundos. Não são aceitos tokens `none` ou HS256.
Um filtro antes do corpo dos recursos resolve a identidade; assinatura e
claims não são decodificadas/validadas por implementação criptográfica própria.
As rotas usam workers para não buscar chaves remotas no event loop.

O identificador interno é UUID v8 derivado de SHA-256 do par inequívoco
`iss`/`sub`. Não depende do e-mail, client ID, nome ou de o subject ser UUID.
Renovar o token mantém o dono; outro subject/issuer não acessa seus recursos.
Os repositórios continuam filtrando listas pelo dono e itens pela lista.

Listas criadas com a identidade fixa de desenvolvimento **não são transferidas**
automaticamente a uma identidade externa. Não editar donos no banco para
contornar autenticação. Mudanças de issuer/subject exigem decisão de migração.

Não há escopos/roles adicionais neste recorte: qualquer token válido para a API
pode operar somente suas próprias listas/itens e consultar código de barras.
Uma lista de outro dono responde 404, sem revelar sua existência.

## Respostas e limites

| Situação | Resposta |
| --- | --- |
| Bearer ausente, inválido, expirado ou não verificável | 401, Problem Details e `WWW-Authenticate: Bearer` |
| Token válido, lista/item não visível ao dono | 404 |
| Auth desligada e identidade fixa desligada | 503, sem acesso ao CRUD |
| Auth desligada e identidade fixa ligada | Demonstração privada, nunca autenticação pública |

Sem chave pública utilizável, a API não aceita a requisição nem recorre à
identidade fixa. Cache/renovação de JWKS são da biblioteca SmallRye; rotação
e disponibilidade do provedor real ainda precisam de validação antes do deploy.
Não registrar token, conteúdo de claims ou detalhes internos de verificação.

## Testes

```sh
cd api
./mvnw -B -Dtest=AutenticacaoHttpTest,ConfiguracaoAutenticacaoTest,IdentificadorExternoTest test
```

Os testes geram RSA temporário em cada execução, assinam tokens sintéticos
e fazem requisições HTTP reais à API. Não usam identidade de segurança
simulada nem chaves privadas reais/versionadas. Repositórios em memória
isolam o contrato HTTP; a suíte PostgreSQL separada continua obrigatória.
Esta suíte não consulta HTTP/JWKS de um fornecedor nem comprova login mobile.

Antes de expor a API:

1. Escolher/configurar o provedor real e obter access tokens de dois usuários.
2. Validar assinatura/JWKS real, issuer, audiência e rotação de chave.
3. Confirmar CRUD autenticado, 401 sem token e 404 entre usuários.
4. Confirmar TLS, banco Neon, migrações e CRUD com Go parado na hospedagem.
5. Registrar URL/commit e evidências sem tokens na #64/#65.

Somente implementar o validador genérico não conclui esses critérios.
