# Cadastro aberto e login próprios

## Contrato

| Método | Rota | Sucesso |
| --- | --- | --- |
| POST | `/api/v1/auth/cadastro` | 201 com `id` e `email` |
| POST | `/api/v1/auth/login` | 200 com `access_token`, `token_type: Bearer`, `expires_in: 1800` |
| GET | `/api/v1/auth/me` | 200 com `id` e `email`; exige Bearer |

Cadastro e login recebem somente `email` e `senha`, sem dono, roles ou
privilégios escolhidos pelo cliente. E-mail ASCII com até 254 caracteres
(parte local até 64), validado com domínio contendo ponto. Normalização:
remoção de espaços nas bordas e conversão integral para minúsculas com
Locale.ROOT. Não remove pontos nem sufixos `+tag`. A unicidade é no PostgreSQL.
E-mails internacionalizados não são suportados neste recorte.

Senha entre 12 e 128 unidades UTF-16 (o tamanho de `String.length()` Java),
sem trim, normalização ou truncamento. Pode conter espaços e Unicode.
Argon2id v19, 19 MiB, duas passagens, uma lane, salt aleatório de 16 bytes e
hash de 32 bytes; senha/hash nunca são retornados pela API.

- 400: JSON inválido; 422: cadastro com entrada inválida.
- 409: e-mail duplicado, inclusive concorrência. Isso permite inferir que
  o e-mail está cadastrado; limitação conhecida e aceita neste recorte.
- 401: login inválido, sem distinguir e-mail inexistente/senha errada.
- 429: limite de tentativas/capacidade; `Retry-After: 60`.
- 503: endpoints locais indisponíveis se `LOCAL_AUTH_ENABLED=false`.

Login e perfil usam `Cache-Control: no-store`; respostas de login também
usam `Pragma: no-cache`. Bearer ausente/inválido mantém Problem Details e
`WWW-Authenticate: Bearer`. Não registrar corpos, senhas ou tokens.

## Configuração no Codespaces

Antes, atualize `develop` quando o PR estiver integrado. O teste deve usar
banco/branch Neon com dados fictícios; iniciar a aplicação aplica Flyway V4,
criando `usuario`. Não execute SQL manual nem altere migrações anteriores.

Gere uma chave persistente fora do repositório, somente uma vez:

```sh
mkdir -p "$HOME/.config/tanalista"
chmod 700 "$HOME/.config/tanalista"
test ! -e "$HOME/.config/tanalista/private.pem"
test ! -e "$HOME/.config/tanalista/public.pem"
```

Se ambos os `test` terminarem com sucesso, execute:

```sh
(umask 077
 openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 \
   -out "$HOME/.config/tanalista/private.pem"
 openssl pkey -in "$HOME/.config/tanalista/private.pem" -pubout \
   -out "$HOME/.config/tanalista/public.pem")
```

Se arquivos já existirem, não os sobrescreva. Proteja/guarde a chave privada
como segredo; não envie seu conteúdo para chat, Git ou logs. Não há geração
automática de chave ao iniciar. A aplicação recusa RSA menor que 2048 bits,
chave inválida ou par assinatura/verificação incompatível.

No `.env` ignorado, além dos valores Neon já configurados:

```dotenv
AUTH_ENABLED=true
LOCAL_AUTH_ENABLED=true
DEV_IDENTITY_ENABLED=false
OIDC_ISSUER=https://tanalista.local/
OIDC_AUDIENCE=tanalista-api
JWT_PRIVATE_KEY_PATH=/CAMINHO_ABSOLUTO/private.pem
OIDC_JWKS_URL=file:///CAMINHO_ABSOLUTO/public.pem
```

Substitua caminhos pelos arquivos criados; `.env` não expande `~`.
`OIDC_ISSUER` é o identificador fixo do emissor, não um endpoint de login.
O nome legado `OIDC_JWKS_URL` também aceita arquivo PEM **somente no modo
local**. Não há fornecedor externo nem chamada a JWKS nesse modo.
Não copie a chave para dentro da imagem Docker: monte segredo no runtime.

Execute:

```sh
docker compose up --build -d barcode
QUARKUS_HTTP_HOST=0.0.0.0 mise run dev
```

O Compose completo continua demonstrativo e aponta para banco local;
o comando acima inicia somente Go e a API no host usa Neon pelo `.env`.
Mantenha 8081 privada e abra `/q/swagger-ui/` pela aba Ports.
Cadastre-se, faça login e cole apenas o access token no **Authorize**.
Use senha fictícia no ambiente de demonstração. Não grave tela com token.

Teste cadastro/login, `/auth/me`, CRUD e outro usuário sem acesso à lista
do primeiro. Pare Go (`docker compose stop barcode`): CRUD e readiness devem
continuar funcionando. `/codigos-barras/{codigo}` falha de forma tratada.
Ao finalizar, Ctrl+C na API, pare Go e pare o Codespace.

## Identidade e limitações

JWT RS256 com issuer, audiência, subject UUID do usuário, iat, exp e jti.
Validade de 30 minutos e tolerância de relógio de 30 segundos. A validação
continua delegada ao SmallRye JWT. O dono da lista é o UUID persistido do
usuário local, não e-mail nem parâmetro enviado pelo cliente.

Não há verificação de e-mail: o endereço é identificador **não verificado**,
sem prova de titularidade. Sem recuperação de senha, envio de e-mails,
refresh token, revogação imediata, exclusão de conta ou MFA nesta entrega.
Logout no cliente apaga seu token; cópia obtida por terceiros permanece
válida até expirar. Rotacionar a chave invalida tokens anteriores.
Dados da identidade fixa e da identidade externa não são transferidos.
Não alternar modos sobre dados reais sem planejar a migração dos donos.

Limiter em memória por IP de conexão e hash do e-mail normalizado, em janela
de 60 segundos: 10 cadastros por IP/5 por e-mail, 30 logins por IP/10 por
e-mail. Sucessos também contam. Capacidade máxima de 10000 chaves e dois
processamentos Argon2 concorrentes; sobrecarga falha com 429.
Reiniciar zera contadores. Não é proteção distribuída contra abuso.
Não confia em X-Forwarded-For: no Codespaces/proxy vários usuários podem
compartilhar o IP e o limite. No deploy real, revisar proxy confiável e
limitação na borda, sem aceitar cabeçalhos arbitrários como identidade.

Cadastro aberto não publica a API: a porta privada restringe quem alcança
o serviço. A exigência acadêmica de hospedagem pública continua pendente.

## Testes e evidências

Unitários: `UsuariosTest`, `LimiteAutenticacaoTest`. Integração:
`CadastroLoginPostgresTest` usa PostgreSQL Testcontainers, chaves efêmeras,
cadastro concorrente, login, perfil, persistência Argon2, CRUD e isolamento.
Nunca acessa Neon real. Testes JWT anteriores continuam verificando
assinatura, claims, expiração e algoritmo inválido.

```sh
mise run test-api
```

Resultado real e CI devem ser registrados no PR; instruções não equivalem
a execução validada no Codespaces com Neon.
