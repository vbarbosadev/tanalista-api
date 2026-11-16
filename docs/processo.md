# Processo de desenvolvimento

## Branches

```text
feature/* ou chore/* -> develop -> main
```

- `develop` é a branch padrão e concentra a integração contínua do trabalho.
- `main` contém somente versões avaliadas ou prontas para produção.
- Mudanças entram por pull request; commits diretos em `main` não fazem parte do fluxo.
- A entrega de cada sprint exige um PR de release de `develop` para `main` com CI verde.
- Usar `feature|test|chore|docs/<issue>-descricao` a partir de `develop`.
- Release usa merge commit para preservar a ancestralidade entre as branches.
- Corrigir diferenças vindas de `main` por PR de sincronização, nunca reset.

## Commits e pull requests

Os commits seguem Conventional Commits, por exemplo `docs: document initial API
contract` e `feat: create shopping list`. Cada pull request deve ter escopo pequeno,
explicar a motivação, indicar como foi validado e relacionar as issues correspondentes.

Revisões verificam comportamento, critérios de aceitação, testes, documentação e ausência
de segredos. O autor só solicita integração quando `mise run ci` passa localmente.

Usar o template de PR com evidências dos comandos realmente executados,
limitações e checklist de autorrevisão. `develop` é a branch padrão:
`Closes #N` em PR para ela pode fechar a issue após merge. Para histórias ainda
incompletas, usar `Refs #N`. Done significa integrado, não necessariamente no ar.

## Definition of Ready

Uma história está pronta para desenvolvimento quando possui:

- formato `Como [papel], quero [ação] para [benefício]`;
- critérios de aceitação verificáveis;
- prioridade, estimativa e sprint no GitHub Project;
- dependências e regras de domínio identificadas;
- dúvidas que impedem a implementação resolvidas.

## Definition of Done

Uma história pode ser fechada quando:

- critérios de aceitação foram atendidos;
- testes relevantes foram adicionados e estão verdes;
- `mise run ci` passa localmente e no GitHub Actions;
- documentação e contrato foram atualizados quando necessário;
- pull request foi revisado e integrado em `develop`;
- não há segredo, artefato local ou ajuste manual necessário.

**Exceção da Sprint 1 (ajuste da disciplina de 13/10):** novos testes unitários,
de integração com banco real e de arquitetura são avaliados na Sprint 2. Não
bloqueiam esta sprint; testes existentes e CI Java/Go continuam obrigatórios.
Validar os critérios com o roteiro de [entrega](entrega-sprint1.md), sem declarar
que o smoke substitui a suíte prevista para Sprint 2. Autorrevisão, evidências e
integração continuam obrigatórias; não marcar Done somente por ter commit/push.

**Projeto individual em DIM0547:** Vinicius Barbosa é o único integrante.
Os PRs passam por autorrevisão, com descrição, checklist e referência às issues;
não se declara revisão externa. Conforme a avaliação da disciplina, para grupos
individuais os indicadores de aprovação por outro integrante e movimentação do
quadro são substituídos por PRs com descrição/checklist e issues fechadas por PR
referenciado. Os demais critérios acadêmicos continuam aplicáveis.
As issues técnicas exigem implementação, CI e integração comprovadas.
A issue do vídeo só é concluída após o link real estar integrado em `main`.
O apoio de IA é registrado em [uso-de-ia.md](uso-de-ia.md).

## Gestão do backlog

O [GitHub Project](https://github.com/users/vbarbosadev/projects/3) é a fonte do backlog.
Prioridades usam P1, P2 e P3; estimativas usam pontos de história na escala de Fibonacci.
O status muda entre `Backlog`, `Ready`, `In progress`, `In review` e `Done`. Fechar uma
issue sem evidência no repositório não é considerado conclusão.

## Sprint 2 — entrega final

O [plano da Sprint 2](SPRINT-2.md) reúne fontes oficiais, escopo, tarefas,
dependências, metas e checklist. Usar o milestone **Sprint 2 — Entrega final**,
com prazo de 30/11/2026 às 23:59 (Fortaleza), responsável, prioridade e
estimativa em cada tarefa. Não somar estimativa da história-pai às subtarefas.

Itens de produto fora do compromisso ficam abertos com `backlog:produto`, sem
Sprint 3/Final acadêmica. Manter preferencialmente um item principal em
execução. Bloqueios devem estar nas relações de dependência das issues.

A exceção de novos testes da Sprint 1 acabou: nesta sprint são obrigatórios
unitários de domínio, integração com PostgreSQL real, arquitetura e fluxo gRPC,
verdes localmente e no CI. O smoke existente não substitui essas suítes.

Proteções de `main` e `develop` devem exigir PR, checks verdes e conversas
resolvidas, sem aprovação externa obrigatória no projeto individual.
Não usar bypass de administrador como fluxo normal.
