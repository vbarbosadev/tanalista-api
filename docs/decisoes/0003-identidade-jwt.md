# ADR 0003 — Identidade externa por JWT e autorização por dono

- Estado: aceita para preparação genérica; provedor e deploy pendentes.
- Data: 17/11/2026.
- Referência: #64.

## Contexto

O CRUD filtra listas por UUID de dono, mas a identidade fixa serve apenas ao
desenvolvimento privado. O responsável autorizou preparar OIDC genérico sem
selecionar fornecedor. Publicar uma identidade compartilhada exporia os dados.

## Decisão

Preparar resource server de access tokens JWT RS256, usando SmallRye JWT
gerenciado pelo BOM Quarkus. Issuer, audiência e JWKS são configurações
obrigatórias quando habilitado; URLs usam HTTPS. O login fica delegado a um
provedor a ser escolhido e integrado pelo cliente, não à API.

Usar par verificado `iss`/`sub` para derivar UUID v8 de SHA-256. A entrada do
hash inclui o tamanho do issuer para evitar pares ambíguos. Os primeiros
128 bits recebem os bits de versão/variante UUID; não armazenar tokens ou
e-mails como identificador de dono. Domínio e casos de uso continuam sem JWT.

Negar credenciais ausentes/inválidas com 401 e desafio Bearer, sem detalhes
internos. Desabilitar autenticação proativa do framework para centralizar
verificação pelo JWTParser e conversão de falhas em Problem Details no
adaptador HTTP. Um filtro pós-matching verifica a identidade antes do corpo
dos recursos; classes bloqueantes permitem JDBC e resolução de chaves.

Auth habilitada não permite fallback à identidade fixa e recusa configuração
incompleta no startup. Auth e identidade fixa desligadas preservam o 503
anterior; modo de demonstração permanece restrito ao ambiente privado.

## Alternativas

- Manter dono fixo publicamente: rejeitado, pois não autentica nem isola usuários.
- Criar cadastro e senhas próprios: aumenta o escopo e a responsabilidade
  de segurança sem necessidade para o recorte acadêmico.
- Exigir subject UUID: rejeitado, pois provedores podem emitir strings opacas.
- Usar e-mail como dono: rejeitado por mutabilidade e dados pessoais.
- Escolher fornecedor agora: adiado por orientação do responsável.
- Usar tokens opacos/introspecção ou fluxo de login OIDC na API: fora do recorte.

## Consequências

- O cliente deve obter access token para a audiência específica da API.
- Não há suporte a login, refresh ou descoberta; não é um provedor OIDC.
- Usuários de demonstração não ganham automaticamente dados autenticados.
- Trocar issuer/subject requer decisão de migração de identidade.
- Testes locais usam chaves RSA efêmeras e repositórios em memória; PostgreSQL
  e provedores reais exigem evidências separadas.
- Esta ADR não substitui a ADR de implantação nem conclui CRUD público da #64.
