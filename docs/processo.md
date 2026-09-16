# Processo de desenvolvimento

## Branches

```text
feature/* ou chore/* -> develop -> main
```

- `develop` é a branch padrão e concentra a integração contínua do trabalho.
- `main` contém somente versões avaliadas ou prontas para produção.
- Mudanças entram por pull request; commits diretos em `main` não fazem parte do fluxo.
- A entrega de cada sprint exige um PR de release de `develop` para `main` com CI verde.

## Commits e pull requests

Os commits seguem Conventional Commits, por exemplo `docs: document initial API
contract` e `feat: create shopping list`. Cada pull request deve ter escopo pequeno,
explicar a motivação, indicar como foi validado e relacionar as issues correspondentes.

Revisões verificam comportamento, critérios de aceitação, testes, documentação e ausência
de segredos. O autor só solicita integração quando `mise run ci` passa localmente.

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

## Gestão do backlog

O [GitHub Project](https://github.com/users/vbarbosadev/projects/3) é a fonte do backlog.
Prioridades usam P1, P2 e P3; estimativas usam pontos de história na escala de Fibonacci.
O status muda entre `Backlog`, `Ready`, `In progress`, `In review` e `Done`. Fechar uma
issue sem evidência no repositório não é considerado conclusão.
