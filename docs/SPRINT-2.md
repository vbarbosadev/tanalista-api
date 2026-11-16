# Sprint 2 — plano de execução e entrega final

**Responsável:** Vinicius Barbosa (`@vbarbosadev`), único integrante DIM0547.
**Prazo:** 30/11/2026, 23:59, horário de Fortaleza.

Este documento organiza trabalho, não declara funcionalidades implementadas.
O [milestone](https://github.com/vbarbosadev/tanalista-api/milestones) e o
[Project](https://github.com/users/vbarbosadev/projects/3) registram a execução.

## Fontes e precedência

Fontes oficiais no repositório da disciplina:

- [Guia da Sprint 2](https://github.com/fmarquesfilho/web2-2026-2/blob/main/docs/SPRINT-2.md).
- [Tarefas T1–T12](https://github.com/fmarquesfilho/web2-2026-2/blob/main/docs/SPRINT-2-TAREFAS.md).
- [Rubricas](https://github.com/fmarquesfilho/web2-2026-2/blob/main/docs/RUBRICAS.md):
  requisitos e pesos técnicos.
- [Avaliação](https://github.com/fmarquesfilho/web2-2026-2/blob/main/docs/AVALIACAO.md):
  notas, atividade, participação e uso de IA.
- [Cronograma](https://github.com/fmarquesfilho/web2-2026-2/blob/main/docs/CRONOGRAMA.md):
  datas e daily meetings.
- [Stack](https://github.com/fmarquesfilho/web2-2026-2/blob/main/docs/STACK.md)
  e [leitura](https://github.com/fmarquesfilho/web2-2026-2/blob/main/leituras/web2-s2.md).

Preferir os documentos oficiais atuais às cópias históricas de
`docs/web2-DIM0547/`. Não existe Sprint 3 acadêmica: Sprint 2 é a entrega final.

## Diagnóstico de partida

Inspeção de 16/11/2026:

- Sprint 1 técnica integrada pelo PR #53; `main` em `57b4759` com CI aprovada.
- `develop` estava atrás de `main`; sincronização pelo PR #54, sem reset.
- Go apenas imprime mensagem de bootstrap; `protos/` ainda sem `.proto`.
- Testes Java existentes cobrem `/hello`; perfil de teste usa H2.
- Compose tem API e PostgreSQL; o Dockerfile depende de build Maven externo.
- Sem evidências de deploy público, licença ou duas ADRs novas.
- Há alterações locais de Sprint 1 não versionadas; não integram este plano.

CI existente não comprova os testes e comportamentos ainda ausentes. O smoke
atual com PostgreSQL é útil e deve ser preservado, mas não substitui toda a suíte.

## Compromisso e limites

Entregar listas e itens com consulta opcional de produtos por código de barras:

```text
Cliente --REST--> API Quarkus --SQL--> PostgreSQL
                       |
                       +--gRPC com deadline--> Go --HTTPS--> provedor externo
```

O diagrama representa a arquitetura-alvo, não o estado já implementado.

O Go valida EAN/UPC e normaliza metadados externos. Não compartilha tabelas,
não mantém catálogo persistente e não cria Item: o usuário confirma os dados
e a criação continua no Quarkus. Um provedor inicial é suficiente; concorrência
adicional precisa ter utilidade real.

Compras (#18), preço/quantidade comprada (#16) e compartilhamento (#15)
permanecem abertos no backlog do produto, fora deste compromisso. Conciliar o
recorte com a equipe mobile. Cache, múltiplas fontes e sincronização offline
também não bloqueiam a entrega acadêmica.

### Decisões antecipadas

- Acesso público seguro é bloqueador: identidade fixa de desenvolvimento não
  autentica usuários. Não publicar o CRUD com `DEV_IDENTITY_ENABLED=true`.
- Escolher identidade/autorização antes do deploy, preferindo autenticação
  delegada sem construir um sistema completo de contas.
- API + Neon no ar; Go inicialmente só no Compose, como permitido pela rubrica.
  Registrar essa decisão na ADR de implantação.
- Proposta para consulta: 422 inválido, 404 ausente, 503 indisponível e 504
  deadline, em Problem Details. Confirmar contrato antes de implementar.
- Listas e itens continuam funcionando sem Go. Não simular sucesso com
  metadados inventados; permitir inclusão manual.
- Health da API não deve falhar apenas porque Go opcional está indisponível.
- Há divergência sobre GHCR: STACK lista publicação no pipeline final; T9
  a considera opcional. Confirmar com o docente e registrar a interpretação.
- Escolha da licença cabe ao responsável, considerando autoria e dependências.

## Backlog rastreável

As tarefas S2-01 a S2-14 correspondem às issues #55 a #68, respectivamente.
Critérios e estado operacional são mantidos nas issues e no Project.

| Item | Entrega | Guia | Meta |
| --- | --- | --- | --- |
| S2-01 | Base, quadro, proteções e processo | T1 | 18/11 |
| S2-02 | ADR da responsabilidade Go | T2 | 18/11 |
| #26 | Contrato, Buf e geração reproduzível | T4 | 19/11 |
| #28 | Domínio Go, erros e context | T3 | 21/11 |
| S2-03 | Provedor HTTP e testes de falhas | T3 | 21/11 |
| S2-04 | Servidor gRPC, health e shutdown | T5 | 22/11 |
| #27 | Porta, cliente Quarkus e integração REST/gRPC | T6 | 23/11 |
| S2-05 | Unitários Java sem infraestrutura | T7 | 22/11 |
| S2-06 | Integração com PostgreSQL e Flyway | T7 | 24/11 |
| S2-07 | ArchUnit e prova de detecção de violação | T7 | 22/11 |
| S2-08 | Docker multiestágio e Compose do zero | T8 | 24/11 |
| S2-09 | CI completo, Buf e sincronia de stubs | T9 | 25/11 |
| S2-10 | Identidade/autorização no ambiente público | T10 | 19/11 |
| S2-11 | Deploy, Neon, migração e ADR | T10 | 25/11 |
| S2-12 | README, OpenAPI, licença e teste por terceiro | T11 | 28/11 |
| S2-13 | Daily e vídeo final | T12 | 29/11 |
| S2-14 | Release e evidências do hash final | Final | 30/11 |

A issue #23 agrega a história de consulta de produto. Não somar estimativa da
história-pai às subtarefas. As tarefas de CI e documentação começam cedo:
as metas indicam conclusão, não autorização para adiar seu início.

### Dependências principais

```text
ADR -> contrato -> domínio/provedor -> servidor Go -> cliente Quarkus
Testes Java -----------------------------------------------> release
Identidade segura + Compose -> deploy -> documentação -----> release
CI cresce junto de cada incremento ------------------------> release
Daily + vídeo --------------------------------------------> release
```

Usar relações `blocked by` nas issues como registro operacional. CI não fica
totalmente bloqueado até o fim das implementações: cresce incrementalmente.
A daily não depende do vídeo final.

## Fluxo no GitHub

1. Consultar milestone **Sprint 2 — Entrega final** e dependências.
2. Mover para Ready quando critérios, responsável e dependências estiverem claros.
3. Criar branch a partir de `develop` atualizada:
   `feature|test|chore|docs/<issue>-descricao`.
4. Manter preferencialmente um item principal em execução; abrir PR pequeno.
5. Usar `Closes #N` para a tarefa resolvida e `Refs #23` para história ainda aberta.
6. Registrar testes realmente executados e limites, não somente comandos sugeridos.
7. Autorrevisar diff, critérios, testes, documentação e segredos.
8. Integrar em `develop` somente com checks verdes e conversas resolvidas.
9. Conferir fechamento e mover cartão para Done.
10. Entregar por PR `develop` -> `main` com merge commit e CI verde.

`develop` é a branch padrão: `Closes #N` nos PRs para ela pode fechar issues.
Done representa integração, não implantação. Vídeo, URL e outros requisitos de
publicação só terminam quando suas evidências estiverem no destino exigido.

Em projeto individual não exigir aprovação de outra conta nem declarar revisão
externa fictícia. PRs devem ter descrição, checklist e autorrevisão. Manter PRs
como caminho normal mesmo para o administrador, sem force push ou exclusão.

### Quadro

`Backlog -> Ready -> In progress -> In review -> Done`.

- Sprint: milestone da entrega; campo Sprint alinhado ao milestone.
- Produto: label `backlog:produto`, sem atribuição à sprint acadêmica.
- Dependências: relações de bloqueio nas issues.
- Revisão: PRs abertos e status In review.
- Entrega: filtro do milestone, excluindo Done.

Visualizações salvas: **Sprint 2**, **Revisão Sprint 2**, **Entrega Sprint 2**
e **Backlog do produto**. As visualizações históricas foram preservadas.
Responsável, prioridade, estimativa e data-meta devem estar preenchidos nas tarefas.

## Verificações por risco

- Go: domínio puro, erros nomeados, `context`, timeout e cancelamento;
  adaptador HTTP com servidor controlado; adaptador gRPC com `bufconn`.
- Contrato: `STANDARD` + `COMMENTS`, stubs gerados e geração reproduzível.
- Java: unitários sem banco; integração com PostgreSQL real; ArchUnit;
  cliente gRPC contra servidor controlado; cenários sem Go.
- Não deixar `*IT` ignorados por `skipITs=true` ou uso exclusivo de `mvn test`.
- Compose: clone limpo, sem `target/` pré-existente, serviços saudáveis.
  Não remover volumes reais do usuário para simular ambiente vazio.
- CI: mesma configuração e cobertura local; preservar smoke existente e
  evidências diagnósticas sem expor segredos.
- Deploy: CRUD seguro, health, banco gerenciado e migração por task/CI.

Os comandos e configurações executáveis serão atualizados nas respectivas
issues. Não apresentar tasks ainda inexistentes como se já funcionassem.

## Marcos e comunicação

| Período | Resultado |
| --- | --- |
| 16–18/11 | Organização, ADR, contrato e decisão de acesso público |
| 19–22/11 | Domínio Go, provedor, gRPC e testes Java |
| 23–25/11 | Fluxo real, daily, Compose e primeiro deploy |
| 26–27/11 | Suíte completa e documentação |
| 28–29/11 | Teste por terceiro, vídeo e release candidata |
| 30/11 | Correções e conferência final antes de 23:59 |

Daily online em 23 ou 25/11, ou presencial em 18/11 com aviso antecipado.
Vídeo de aproximadamente cinco minutos: responsabilidade do Go, requisição
pública e degradação, contrato/deadline, Compose e testes/CI.

Atividade: commits em pelo menos metade das semanas, PR autorado integrado,
PRs com descrição/checklist e issues fechadas por PR. Não fabricar datas para
compensar semanas sem contribuição. Consultar o docente sobre aplicação do
Fator de Participação a integrante único: a seção de saúde do repositório traz
substituições explícitas, mas não autoriza simular revisão alheia.

## Checklist de fechamento

- [ ] Go útil, domínio puro, context e erros corretos.
- [ ] Contrato comentado, Buf verde e stubs gerados.
- [ ] Teste automatizado do fluxo gRPC e do prazo.
- [ ] Compose do zero com API, Go e banco saudáveis.
- [ ] Unitários, PostgreSQL e arquitetura verdes local e no CI.
- [ ] API pública e OpenAPI; CRUD seguro funciona sem Go.
- [ ] Migração por task/CI e segredos fora do Git.
- [ ] README com diagrama, execução, testes e exemplos.
- [ ] Duas ADRs novas e LICENSE aprovada pelo responsável.
- [ ] Pessoa externa roda em menos de dez minutos, com registro.
- [ ] Daily realizada e vídeo acessível integrado ao README.
- [ ] Registro de IA atualizado; nenhuma revisão/participação fictícia.
- [ ] Release `develop` -> `main`, CI verde e hash final registrado.

Guardar os links de PR, workflow, deploy e vídeo na issue de release. Não
marcar itens concluídos por existir apenas planejamento ou código não validado.
