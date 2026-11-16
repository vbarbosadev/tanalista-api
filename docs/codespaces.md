# Executar no GitHub Codespaces

O Codespaces é o ambiente de desenvolvimento adotado para esta etapa.
O repositório fornece `.devcontainer/devcontainer.json`, Docker-in-Docker e
setup com mise, Java 21, Go 1.24, Buf 1.50 e Node 22. Não exige instalar
essas linguagens ou Docker na máquina pessoal.

## Abrir e executar

1. No GitHub, selecione **a branch `develop`**, depois
   **Code -> Codespaces -> Create codespace**.
2. Use uma máquina com 4 CPUs e 8 GB de memória, se disponível. Há consumo de
   quota; criar, manter e publicar um Codespace depende da conta do responsável.
3. Espere o `postCreateCommand` concluir. O setup baixa uma versão fixa do
   mise, confere SHA256 e instala as versões de `mise.toml`.
4. No terminal:

   ```sh
   docker info
   mise run ci
   docker compose up --build
   ```

5. Em outro terminal, confira `docker compose ps`. Abra a porta **8081**
   na aba **Ports**, mantendo a visibilidade **Private**.
6. Acesse `/q/swagger-ui/`, `/q/openapi` e `/q/health/ready` nessa URL.
   O gRPC é interno na 9090; PostgreSQL é interno na 5432. Não publicar essas portas.

Se o devcontainer já existia antes desta configuração, use **Codespaces:
Rebuild Container**. O setup não inicia a aplicação automaticamente nem apaga
dados. Para encerrar apenas a aplicação, `docker compose down` preserva volume.
Ao terminar o trabalho, pare o Codespace para limitar uso de quota.

## Testes e demonstração sem depender do provedor real

```sh
bash .devcontainer/verify.sh
```

Esse comando roda a CI de linguagens, inclusive PostgreSQL Testcontainers,
depois usa um projeto Compose isolado com fixture HTTP, faz consultas, para
Go e repete health/CRUD. Não executar junto do Compose normal nas mesmas
portas. O script encerra somente seu próprio projeto, sem `down -v`.
Os logs usam dados sintéticos; não versioná-los nem substituir a fixture
por dados sensíveis de produção.

Teste de banco roda no mesmo `mise run ci` local e no CI; ausência de Docker
é uma falha, não motivo para trocar PostgreSQL por H2 ou pular a suíte.
Unitários isolados continuam disponíveis conforme [testes.md](testes.md).

## Segurança e publicação

O Compose local habilita uma identidade fixa de demonstração. **Isso não é
autenticação de usuários.** Uma porta privada do Codespaces limita acesso à
conta GitHub autorizada, mas não implementa isolamento entre usuários do produto.
Não mudar a visibilidade para Public com a configuração atual.
Não compartilhar cookies, tokens, `.env`, credenciais ou terminal autenticado.

Uma URL encaminhada do Codespaces depende do Codespace ativo e pode deixar de
responder após suspensão; não é hospedagem permanente. Senhas reais pertencem
a Codespaces Secrets ou ao segredo da plataforma, nunca ao Git.

O [guia oficial da Sprint 2](https://github.com/fmarquesfilho/web2-2026-2/blob/main/docs/SPRINT-2.md)
também exige API pública e banco gerenciado. Rodar no Codespaces não demonstra
automaticamente esse requisito. A #65 permanece pendente até confirmar com o
docente se há exceção específica, ou implantar conforme a rubrica.
Neon, se adotado, não substitui o PostgreSQL descartável dos testes.

## O que está validado

O workflow valida o mesmo devcontainer por `devcontainers/ci`, sem criar um
Codespace na conta do usuário. Resultado verde comprova build e execução desse
ambiente no Actions, **não** acesso anônimo à URL, teste manual de navegador,
quota disponível ou criação de Codespace na conta.
Registrar separadamente a execução real no Codespaces antes da apresentação.
