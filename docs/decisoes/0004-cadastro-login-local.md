# ADR 0004 — Cadastro aberto com e-mail e senha

- Data: 17/11/2026.
- Estado: aceita para implementação; validação manual e deploy pendentes.
- Referência: #64.
- Substitui a delegação de login da ADR 0003 no modo local; preserva modo externo.

## Contexto

O responsável optou por autenticação própria, sem fornecedor externo,
cadastro aberto somente com e-mail e senha e Codespaces como hospedagem
temporária. A API já valida JWT RS256 e isola listas por dono.

## Decisão

Persistir usuários com e-mail normalizado único, hash Argon2id e UUID;
expor cadastro, login e perfil. Emitir JWT RS256 de 30 minutos com chave
persistente fora do Git e validar pelo SmallRye JWT existente.
O modo local exige auth habilitada e identidade fixa desabilitada.
Verificar o par RSA na inicialização, sem gerar chave silenciosamente.

Usar UUID do usuário como subject e dono no modo local. Tokens externos
mantêm o mapeamento issuer/subject da ADR 0003; trocar modos não migra donos.
Domínio e aplicação usam interfaces para repositório e hash, sem framework.

Limitar cadastro/login por IP e e-mail com contadores em memória limitados,
e limitar concorrência de hash para a instância única. Não confiar em
cabeçalhos de proxy sem política de proxies confiáveis.

## Alternativas

- Provedor externo: implementação genérica preservada, mas dispensado pelo
  responsável para este recorte.
- Usuários de demonstração pré-criados: descartado em favor de cadastro aberto.
- Senhas em texto, hashes rápidos ou salt fixo: rejeitados.
- Refresh tokens e recuperação de senha: adiados para não ampliar a superfície
  de credenciais persistentes e envio de e-mails nesta entrega.

## Consequências

- A API assume armazenamento e proteção de credenciais, não somente validação.
- E-mail não é verificado; não comprova titularidade nem recebe recuperação.
- 409 em cadastro permite inferir existência de e-mail, risco documentado.
- Limiter é local e reinicia junto do processo; não resolve abuso distribuído.
- Logout não revoga token; ele expira após 30 minutos (mais tolerância de relógio).
- Chave precisa de backup e gestão de segredo fora do repositório.
- Não transfere listas da identidade fixa nem conclui hospedagem pública.
- Contrato, parâmetros e passos operacionais: [autenticacao-local.md](../autenticacao-local.md).
