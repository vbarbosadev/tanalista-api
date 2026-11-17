# Imagens no GitHub Container Registry

## Quando acontece

O workflow `CI` publica imagens somente em **push na branch `main`**, após
`Build, test and lint` e `Compose, migrations and CRUD smoke` passarem.
PRs e pushes em `develop` não fazem login nem publicam pacotes.
Os checks de Codespaces continuam no workflow separado e devem estar verdes
antes do merge do PR conforme o processo de autorrevisão.

A publicação usa `GITHUB_TOKEN`, sem PAT pessoal. Somente o job de publicação
tem `packages: write`; os outros jobs mantêm `contents: read`.
Não são usados segredos Neon no build. `.dockerignore` exclui `.env` e
arquivos de ambiente; Dockerfiles copiam somente os fontes necessários.

## Nomes e versões

| Serviço | Imagem |
| --- | --- |
| API Java | `ghcr.io/vbarbosadev/tanalista-api-api:<SHA_COMPLETO_DE_MAIN>` |
| Consulta Go | `ghcr.io/vbarbosadev/tanalista-api-barcode:<SHA_COMPLETO_DE_MAIN>` |

Cada execução usa o SHA completo do commit, sem atualizar uma tag `latest`.
O resumo do job registra a tag e o digest retornado pelo registro, e consulta
o manifesto remoto após o push. Para uma implantação reproduzível, use o
digest `imagem@sha256:...`, não um endereço inventado antes da publicação.
Reexecutar o mesmo commit pode atualizar sua tag ao reconstruir imagens base;
o digest identifica o conteúdo exato.

Publicar uma imagem **não inicia a aplicação**, não publica portas e não
conecta automaticamente ao Neon. A API continua sem identidade fixa habilitada
por padrão na imagem; as variáveis de runtime pertencem à plataforma.

## Primeira publicação e acesso

A primeira publicação ocorrerá após o PR final `develop -> main`, não ao
integrar esta configuração em `develop`. Até lá, o build é comprovado pelo
smoke Compose, mas existência de pacotes e permissões do GHCR não estão
comprovadas. A issue #63 deve manter essa distinção.

Se o push retornar erro de permissão, confira as políticas de Actions/Packages
e se o pacote preexistente concede acesso ao repositório. Não substitua
automaticamente o token por credencial pessoal nem amplie permissões globais.

Novos pacotes podem ser privados mesmo num repositório público. O responsável
deve conferir a visibilidade em **Packages -> Package settings** e, se a
hospedagem precisar baixar sem credenciais, autorizar visibilidade pública.
O workflow não altera a visibilidade nem fornece tokens à hospedagem.

Depois da publicação e da autorização de acesso, obtenha a imagem:

```sh
docker pull ghcr.io/vbarbosadev/tanalista-api-api:<SHA_COMPLETO_DE_MAIN>
docker pull ghcr.io/vbarbosadev/tanalista-api-barcode:<SHA_COMPLETO_DE_MAIN>
```

Substitua o placeholder por um SHA que realmente apareça num job aprovado.
Não execute os placeholders literalmente.

## Exigência acadêmica

T9 chama a publicação de opcional; STACK a lista no pipeline mínimo.
Foi preparada a publicação para atender à interpretação mais abrangente,
sem afirmar que o docente respondeu à consulta. A rubrica continua exigindo
API pública e banco gerenciado; guardar imagens no GHCR não cumpre o deploy.
