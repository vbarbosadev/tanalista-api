# Evidências e fechamento da Sprint 1

Prazo: **16/10/2026, 23h59**. A entrega avaliada é o commit em `main`, não o
estado de uma branch de funcionalidade. Este documento não declara revisão,
merge ou vídeo realizados enquanto faltarem evidências.

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
  ao job específico no GitHub Actions; conferir o resultado antes do merge.

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

## Integração e participação — pendências humanas

Os PRs são empilhados, cada um com commit e push de uma issue. Revisar e integrar
na ordem: domínio → arquitetura → persistência → criar listas → consultar listas
→ atualizar listas → criar itens → unicidade → atualizar itens → consultar itens
→ Problem Details → OpenAPI → validação da entrega → roteiro do vídeo.

Ao integrar uma base, atualizar a base do próximo PR para `develop`, sem squash
dos commits que são ancestrais dos próximos PRs. Usar merge commit preserva a
cadeia e facilita a revisão incremental. Não fazer force-push nem apagar bases
enquanto existirem PRs dependentes. As issues só vão para Done após revisão e
integração; autoria automatizada não vale como participação de outro integrante.

- [ ] Outro integrante revisou os PRs; verificar comentários/aprovações reais.
- [ ] Cada integrante possui ao menos um PR próprio integrado nesta sprint.
- [ ] PRs integrados em `develop`, com CI verde e cartões vinculados.
- [ ] Vídeo da Sprint 1 gravado por todos, acessível e linkado no README (#36).
- [ ] PR de release `develop` → `main` revisado e integrado (#35).
- [ ] CI verde no commit de `main`; registrar hash e URL do workflow em #35.

Na inspeção inicial, `main` tinha um merge commit exclusivo e `develop` três
commits exclusivos (definição do serviço Go e vídeo da Sprint 0). Isso é uma
divergência de histórico, não autorização para descartar mudanças. O PR de
release deve integrar ambos e confirmar a ausência de conflitos.

Somente o proprietário apareceu na lista de colaboradores consultada. A equipe
precisa garantir acesso para revisão/contribuições ou usar forks e PRs públicos.
Não foram adicionados colaboradores nem simuladas aprovações de outras contas.
