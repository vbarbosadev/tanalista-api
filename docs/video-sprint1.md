# Roteiro do vídeo — Sprint 1 (5 minutos)

**Estado: roteiro preparado; vídeo ainda não gravado/publicado.** A issue #36
só termina quando o grupo publicar um link acessível no README. Não utilizar
o link da Sprint 0 como evidência desta sprint.

## Preparação

- Integrar PRs revisados e executar a versão que será entregue em `main`.
- `mise run build-api`, `mise run up` e `node scripts/smoke-sprint1.mjs`.
- Abrir Swagger UI em `http://localhost:8081/q/swagger-ui/`, Actions do commit
  demonstrado, migrações e pacotes `dominio`, `aplicacao` e `adaptadores`.
- Criar dados demonstrativos próprios; não mostrar `.env`, senhas, tokens ou
  informações privadas na gravação. Não apagar dados de terceiros para gravar.
- Combinar falas. A divisão abaixo é sugestão e precisa ser confirmada pela equipe.

## Falas e telas

| Tempo | Pessoa sugerida | Tela e demonstração |
| --- | --- | --- |
| 0:00–0:30 | Vinicius | Explicar que a sprint entrega CRUD de Lista e Item com PostgreSQL e relacionamento 1:N. Identidade temporária não é autenticação real. |
| 0:30–2:00 | Vinicius | Swagger: POST Lista 201/Location; POST Item vinculado; GET individual e páginas com `size=1`, filtro de nome/categoria; PATCH e DELETE 204. Mostrar que operações estão documentadas. |
| 2:00–3:00 | Ivis | Compose: banco/API e volume; Flyway V1–V3, FK e índices únicos. Reiniciar API e consultar dado persistido. Esquema gerado somente por migrações. |
| 3:00–4:00 | Thallys | Domínio e aplicação sem imports de framework; portas de repositório; adaptadores HTTP/JPA e implementação em memória. Diferenciar legado Produto do CRUD novo. |
| 4:00–4:35 | Thallys | Nome vazio 422, UUID inválido 400, duplicidade 409 e inexistente 404. Mostrar Content-Type e campos RFC 9457. |
| 4:35–5:00 | Ivis | CI verde no hash entregue, PRs/issues vinculados e pendências da Sprint 2: testes de domínio/integração/arquitetura, compras e gRPC. |

## Sequência curta no Swagger

1. `POST /api/v1/listas`: `{"nome":"Vídeo Sprint 1"}`. Copiar o UUID e mostrar
   `Location`; usar nome novo se a demonstração já foi executada antes.
2. `POST /api/v1/listas/{listaId}/itens`: `{"nome":"Café","quantidade":2,
   "unidade":"UNIDADE","categoria":"ALIMENTOS","precoEstimado":6.50}`.
3. Adicionar `Leite 200g` e `Leite 400g`, demonstrando que permanecem distintos.
4. Listar itens com `page=0`, `size=1`, `nome=leite`, `categoria=ALIMENTOS`
   (informar a categoria também na criação desses dois itens para usar esse filtro).
5. Consultar pelo UUID. PATCH `{"quantidade":3}` mantém os campos ausentes;
   PATCH `{"precoEstimado":null}` limpa somente o valor opcional.
6. Tentar criar `"  CAFE  "`: deve retornar 409 para a lista que já contém Café.
7. `POST /listas` com nome vazio: 422. Consultar UUID inválido: 400; UUID bem
   formado inexistente: 404. Não apresentar entradas inválidas como erro 500.
8. Antes de excluir a lista, reiniciar a API e consultar o mesmo UUID para
   evidenciar persistência. Depois DELETE Item e Lista, mostrando 204 sem corpo.

## Publicação e conclusão (manual)

- [ ] Todos os integrantes falaram; áudio/tela legíveis e duração próxima de 5 min.
- [ ] Link funciona em janela anônima, sem pedir acesso individual.
- [ ] Adicionar link real no início do README, na seção Vídeos.
- [ ] Abrir PR próprio da publicação, vinculado a #36, com revisão de outro integrante.
- [ ] Integrar antes da release final e registrar o link na issue #36.

O roteiro, por si só, não cumpre o critério do vídeo nem o PR próprio de todos os
integrantes. A revisão humana e as contribuições reais da equipe são necessárias.
