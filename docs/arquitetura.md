# Arquitetura do CRUD

- `tanalista.dominio`: records imutáveis, invariantes e portas `ListaRepository`
  e `ItemRepository`; somente imports da biblioteca padrão do Java.
- `tanalista.aplicacao`: casos de uso sem dependência de framework.
- `tanalista.adaptadores.http`: recursos, DTOs, transações e tradução de erros.
- `tanalista.adaptadores.banco`: entidades JPA e implementações das portas.
- `tanalista.adaptadores.memoria`: implementações alternativas sem CDI, úteis
  para testes na Sprint 2. A composição deve compartilhar os mesmos repositórios.
- `tanalista.config`: composição CDI e identidade temporária de desenvolvimento.

O HTTP depende dos casos de uso, que dependem exclusivamente das portas do domínio.
Entidades JPA nunca saem do adaptador de banco. Paginação do adaptador PostgreSQL
é executada por Panache/SQL; somente o adaptador em memória pagina coleções.
Transações nos métodos de escrita HTTP abrangem os casos de uso e o `flush`,
incluindo alteração do item e atualização da lista.

O código anterior de Produto (`entity`, `repository`, `service`, `controller`)
fica isolado como legado, preservado para não descartar trabalho ou dados. Não
é o domínio do novo CRUD, não participa de Lista/Item e não deve ser apresentado
como exemplo da separação nova. Uma retirada futura exige migração e decisão
explícita da equipe. A migração V1 não deve ser editada.

Conferência manual nesta sprint: inspecionar os imports de `dominio` e `aplicacao`.
Teste automatizado de arquitetura e novos testes obrigatórios ficam para Sprint 2.
