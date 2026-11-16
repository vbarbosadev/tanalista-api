# Evidências e fechamento da Sprint 1

Prazo: **16/10/2026, 23h59**. A entrega avaliada é o commit em `main`, não o
estado de uma branch de funcionalidade. Atualização: **16/10/2026**.

## Identificação e situação atual

- **Único integrante em DIM0547:** Vinicius Barbosa, matrícula 20230051760,
  GitHub [vbarbosadev](https://github.com/vbarbosadev).
- **Vídeo:** [demonstração da Sprint 1](https://youtu.be/arCPkAo0HdQ), informado
  pelo responsável. O conteúdo e o acesso anônimo não foram avaliados nesta revisão.
- **Release técnica:** [PR #53](https://github.com/vbarbosadev/tanalista-api/pull/53),
  integrado em `main`, commit `cc74accb5f1efc781c0d62825811c7d874e12a1b`.
- **CI desse commit:** [execução aprovada](https://github.com/vbarbosadev/tanalista-api/actions/runs/38740126762),
  com build/test/lint Java/Go e Compose, migrações, CRUD, isolamento e persistência.
- **Uso de IA como auxílio:** [registro de ferramentas e atividades](uso-de-ia.md).

A implementação técnica já está em `main`. O link do vídeo e as correções deste
fechamento estão preparados localmente e ainda exigem commit, PR, integração e
CI do novo commit. A aprovação de `cc74acc` não valida alterações posteriores.

## Validação técnica reproduzível

1. Configure `.env` a partir de `.env.example`, apenas em ambiente confiável.
2. `mise run ci`: compila Java/Go e executa as verificações existentes.
3. `mise run build-api` e `mise run up`: empacota a API e sobe API/PostgreSQL.
4. Com Node 22+ instalado: `mise run smoke`, ou `node scripts/smoke-sprint1.mjs`.
   Outra porta: configure `API_URL`, por exemplo `http://localhost:18081`.

O roteiro automatizado usa somente bibliotecas nativas do Node e cria listas
com identificador aleatório. Na limpeza, remove **somente suas próprias listas**.
Não executa truncamento, limpeza global ou alteração de dados preexistentes.

Verifica os dois CRUDs, UUID/Location, relação Lista–Item, paginação/filtros,
normalização (caixa, acento, espaços), nomes distintos por embalagem, duplicidade
concorrente, PATCH ausente versus null, limites numéricos/textuais, parâmetros
vazios/repetidos/desconhecidos, erros RFC 9457, isolamento entre listas e
OpenAPI/Swagger. Testes novos de domínio, integração e arquitetura continuam
na Sprint 2; este smoke é uma verificação de entrega, não substitui aquela suíte.

O job `sprint1-smoke` do GitHub Actions também valida Compose, inicialização em
banco vazio, migrações, identidade desabilitada, isolamento entre dois donos e
persistência após recriar a API. O workflow anterior Java/Go permanece ativo.

## Evidência local em 15/10/2026

- `mise run ci`: passou (build Java/Go, teste HTTP existente e verificações).
- API empacotada, perfil `prod`, PostgreSQL 17.11 local em schema isolado
  `sprint1_validation_20261015`; nenhuma tabela preexistente foi alterada.
- Flyway aplicou V1/V2 em schema vazio e depois V3; reinícios validaram os
  checksums sem reaplicar migrações. V1 original foi preservada.
- Smoke CRUD: **86 requisições verificadas**, incluindo duas escritas concorrentes
  com resultado 201/409, sem retorno 500.
- Modo `--disabled`: 2 requisições verificadas, sem habilitação da identidade.
- Modo `--hidden`: 6 requisições verificadas usando outro UUID de dono.
- Persistência: `--seed` cria lista/item; `--verify` consulta os mesmos dados
  após reiniciar a API e remove somente a lista semeada: 3 requisições verificadas.
- Docker não está instalado no ambiente local. Validação de Compose é delegada
  ao job específico no GitHub Actions.

## Evidência no GitHub Actions

[CI completa do commit b14ade8](https://github.com/vbarbosadev/tanalista-api/actions/runs/38736029541)
passou em 15/10: build/test/lint de Java/Go e job `Compose, migrations and CRUD
smoke`. Este último construiu a imagem, iniciou PostgreSQL 16 e API, executou
CRUD/validações/OpenAPI, semeou dados, desabilitou a identidade, trocou o dono e
confirmou persistência ao recriar a API. Isso valida Compose mesmo sem Docker local.

[CI da documentação final 18879e1](https://github.com/vbarbosadev/tanalista-api/actions/runs/38736229736)
também passou nos dois jobs. Markdownlint dos oito documentos modificados passou.
Esses resultados históricos são das branches de entrega. A CI de `main` após
a release está indicada na seção de situação atual.

| Issue | PR incremental | Commit da implementação |
| --- | --- | --- |
| #25 | [#37](https://github.com/vbarbosadev/tanalista-api/pull/37) | `61f5ab8` |
| #29 | [#38](https://github.com/vbarbosadev/tanalista-api/pull/38) | `ed5e9fc` |
| #30 | [#39](https://github.com/vbarbosadev/tanalista-api/pull/39) | `d1fe966` |
| #19 | [#40](https://github.com/vbarbosadev/tanalista-api/pull/40) | `c7e9769` |
| #17 | [#41](https://github.com/vbarbosadev/tanalista-api/pull/41) | `70e40dc` |
| #31 | [#42](https://github.com/vbarbosadev/tanalista-api/pull/42) | `4329bb4` |
| #14 | [#43](https://github.com/vbarbosadev/tanalista-api/pull/43) | `1c3bc32` |
| #12 | [#44](https://github.com/vbarbosadev/tanalista-api/pull/44) | `dda13a9` |
| #13 | [#45](https://github.com/vbarbosadev/tanalista-api/pull/45) | `31b294a` |
| #32 | [#46](https://github.com/vbarbosadev/tanalista-api/pull/46) | `064f191` |
| #33 | [#47](https://github.com/vbarbosadev/tanalista-api/pull/47) | `6022a76` |
| #34 | [#48](https://github.com/vbarbosadev/tanalista-api/pull/48) | `d2c2036` |
| #35 (parcial) | [#49](https://github.com/vbarbosadev/tanalista-api/pull/49) | `b14ade8` |
| #36 (roteiro) | [#50](https://github.com/vbarbosadev/tanalista-api/pull/50) | `18879e1` |

Os testes revelaram e corrigiram categoria vazia interpretada como ausência e
tradução de unicidade concorrente dependente do idioma do PostgreSQL. A tradução
agora usa o nome da restrição nos metadados do protocolo do driver.

## Conferência manual para o vídeo

- Swagger UI `/q/swagger-ui/`: criar duas listas, listar com `size=1`, filtrar,
  consultar ID, renomear e excluir.
- Adicionar itens à lista e mostrar `listaId`; buscar, filtrar por nome/categoria,
  paginar, editar e remover. Demonstrar que Item de outra lista retorna 404.
- Mostrar 201/Location, 204 sem corpo, 400/422 de entrada e 409 de duplicidade,
  sempre com `application/problem+json` nos erros.
- Mostrar migrações V1–V3 e `schema-management.strategy=none`; reiniciar a API
  e confirmar que os dados continuam no PostgreSQL.
- Inspecionar imports de `dominio` e `aplicacao`: apenas `java.*` e domínio.
  Adaptadores JPA e HTTP ficam fora dessas camadas.
- Não apresentar Produto legado como CRUD entregue, identidade temporária como
  autenticação, nem compras/progresso/compartilhamento como implementados.

## Histórico de integração — registro individual

**Atualização de 15/10:** o responsável informou não contar com outro integrante
e autorizou expressamente os merges após checks verdes, sem revisão externa.
Não houve aprovação de terceiros e não se presume dispensa da rubrica pelo
professor. Os PRs #37–#50 estão integrados em `develop`; o PR #51 trouxe os
merges #38/#39 que haviam entrado em bases intermediárias. Nenhum histórico foi
descartado e nenhuma branch foi apagada.

O fechamento técnico de #35 já ocorreu após a release #53 e a CI verde de `main`.
A #36 permanece aberta até a integração do link do vídeo na entrega.
Após publicar estas alterações em nova release, atualizar o hash final e a CI
na #35 e no canal de entrega adotado pela disciplina. O roteiro de preparação
é mantido apenas localmente e não faz parte dos arquivos versionados da entrega.

### Procedimento original de integração e critérios da rubrica

Os PRs são empilhados, cada um com commit e push de uma issue. Revisar e integrar
na ordem: domínio → arquitetura → persistência → criar listas → consultar listas
→ atualizar listas → criar itens → unicidade → atualizar itens → consultar itens
→ Problem Details → OpenAPI → validação da entrega → roteiro do vídeo.

Ao integrar uma base, atualizar a base do próximo PR para `develop`, sem squash
dos commits que são ancestrais dos próximos PRs. Usar merge commit preserva a
cadeia e facilita a revisão incremental. Não fazer force-push nem apagar bases
enquanto existirem PRs dependentes. As issues só vão para Done após revisão e
integração; autoria automatizada não vale como participação de outro integrante.

- Único integrante do backend: **Vinicius Barbosa**. Não há revisão externa
  nem participação atribuída à equipe do aplicativo mobile nesta entrega.
- Para grupos individuais, a avaliação substitui os indicadores de aprovação
  por outro integrante e movimentação do quadro por PRs com descrição/checklist
  e issues fechadas por PR referenciado. Os demais critérios permanecem válidos.
- [x] PRs integrados em `develop`, com checks verdes e cartões vinculados.
- [x] Vídeo pronto e link do YouTube informado pelo responsável.
- [x] Link incluído no README local e registro de uso de IA criado.
- [x] Release técnica `develop` → `main` integrada pelo PR #53 (#35).
- [x] CI verde no commit técnico de `main`, com hash e workflow registrados em #35.
- [ ] Confirmar acesso ao vídeo em janela anônima e conferir duração/legibilidade.
- [ ] Integrar documentação e link do vídeo via PR em `develop` e release para `main`.
- [ ] Aguardar CI verde no novo commit de `main` e atualizar hash/links em #35.
- [ ] Fechar #36 por PR referenciado após integração e concluir seu cartão no quadro.
- [ ] Confirmar registro da daily de 28 ou 30/09 e composição individual com o professor.

Na inspeção inicial, `main` tinha um merge commit exclusivo e `develop` três
commits exclusivos (definição do serviço Go e vídeo da Sprint 0). Isso é uma
divergência de histórico, não autorização para descartar mudanças. O PR de
release deve integrar ambos e confirmar a ausência de conflitos.

Somente o proprietário apareceu na lista de colaboradores consultada, coerente
com o projeto individual. Não foram adicionados colaboradores nem simuladas
aprovações de outras contas. O histórico acima não representa pendência de
convidar integrantes para esta entrega.
