# ADR 0002 — Ambiente de desenvolvimento no GitHub Codespaces

- Estado: aceita para desenvolvimento e testes.
- Data: 17/11/2026.
- Referências: issues #79, #64 e #65; PR #80.

## Contexto

O responsável determinou que todo o ambiente rode no GitHub Codespaces, como
requisito da disciplina. A API Java, serviço Go e PostgreSQL precisam executar
juntos, com ferramentas e testes reproduzíveis. A máquina Windows usada pela
IA não possui Docker; seus testes de linguagens não comprovam containers locais.

## Decisão

Versionar devcontainer Ubuntu com Docker-in-Docker e instalar Java, Go, Buf e
Node pelo `mise.toml`. O instalador mise usa versão fixa e verificação SHA256.
Docker-in-Docker 4.1.3 seleciona iptables no início conforme o kernel; a
verificação aguarda o daemon por tempo limitado e falha com diagnóstico.

Usar o mesmo devcontainer no Actions. `bash .devcontainer/verify.sh` executa
as suítes e um Compose isolado com provedor HTTP sintético, incluindo CRUD e
readiness com Go parado. Testcontainers usa PostgreSQL descartável, não Neon.

Manter a porta 8081 privada e não encaminhar automaticamente banco ou gRPC.
A identidade fixa do Compose serve somente à demonstração local.

## Alternativas

- Instalação manual na máquina pessoal: não atende à orientação de ambiente
  principal no Codespaces e amplia diferenças entre máquinas.
- Usar somente Actions: oferece regressão automatizada, mas não substitui
  desenvolvimento e validação real no Codespaces.
- Tratar Codespaces como deploy público: rejeitado como conclusão automática;
  suspensão, identidade de demonstração e exigência de banco gerenciado
  precisam ser tratados separadamente.

## Consequências

- Não é necessário instalar toolchains na máquina pessoal.
- Reconstruir o container é necessário após mudanças de configuração.
- Há consumo de quota; o responsável deve parar o Codespace quando não usar.
- Testes exigem Docker; sua ausência não é mascarada por H2 ou skip.
- A execução automatizada e o relato manual do responsável estão ligados em
  [codespaces.md](../codespaces.md), com os limites de cada evidência.
- Esta decisão não escolhe autenticação nem plataforma de produção, não
  dispensa API pública/Neon e não conclui #64 ou #65.
