# Uso de IA como auxílio ao desenvolvimento

## Responsável e ferramenta

O projeto de backend em DIM0547 é individual, de **Vinicius Barbosa**
(matrícula 20230051760, GitHub `vbarbosadev`). Foi utilizado o assistente de IA
**OpenAI no OpenCode** como ferramenta de apoio durante a Sprint 1.

## Atividades apoiadas

- Consulta e interpretação dos critérios da disciplina e comparação com as
  evidências disponíveis no repositório.
- Apoio à implementação e aos ajustes de código, configuração e contratos,
  incluindo sugestões e edição de arquivos.
- Apoio à preparação e execução de verificações, análise de erros e organização
  das evidências de CI, persistência, validação e CRUD.
- Auxílio na organização de issues, pull requests e integração das alterações,
  mediante orientação e autorização do responsável.
- Elaboração e revisão de documentação, checklists e roteiro de demonstração;
  atualização dos links e do registro de entrega.

## Limites e responsabilidade

A IA foi utilizada como auxílio, não como integrante da equipe ou revisora humana
independente. Seu uso incluiu sugestões e alterações em arquivos, não apenas
consultas conceituais. Este registro não atribui toda a construção do projeto à
ferramenta nem declara autoria ou participação de terceiros.

As decisões de escopo, a aceitação das alterações e a responsabilidade pela
entrega são de Vinicius. O código submetido deve ser compreendido e explicado
pelo responsável. Sugestões da ferramenta não substituem validação por execução,
evidências de CI ou os critérios de avaliação da disciplina.

## Atualização de 16/10/2026

Nesta etapa, a IA auxiliou na conferência do fechamento da Sprint 1, na correção
da composição individual, neste registro de uso e na inclusão do vídeo informado
pelo responsável. A atualização documental não representa nova execução dos
testes nem avaliação do conteúdo do vídeo.

## Atualização de 16/11/2026 — planejamento e GitHub da Sprint 2

O assistente OpenAI no OpenCode auxiliou na leitura dos guias atuais da
disciplina, inspeção do código e do GitHub, definição de escopo, organização
de milestone, issues, dependências e Project, sincronização das branches por
PR e preparação deste fluxo e dos templates. As operações foram autorizadas
pelo responsável após apresentação do plano.

Não foi implementado o serviço Go, autenticação, suíte nova de testes ou
deploy nesta etapa. Os resultados de Actions são evidências da suíte existente,
não comprovação dos critérios ainda pendentes da Sprint 2. Autorrevisão apoiada
por IA não é aprovação externa nem contribuição de outro integrante.

## ADR do serviço Go — issue #56

Em 16/11/2026, o assistente auxiliou na elaboração da ADR 0001, na comparação
com a alternativa dentro do Quarkus, na delimitação do provedor inicial e na
separação entre decisão arquitetural e implementação pendente. Também apoiou
lint documental, autorrevisão e condução do PR conforme autorização do responsável.
Não houve consulta real ao provedor nem implementação de Go/gRPC nesta tarefa.

## Contrato Protobuf — issue #26

O assistente auxiliou na definição do contrato v1, presença de campos opcionais,
semântica de erros, configuração Buf e geração de stubs Go/Java a partir de
uma fonte única. Também apoiou testes de mensagens geradas e verificações de
sincronia no CI. Isso não representa implementação do provedor ou do fluxo
de consulta. Comandos e resultados executados são registrados no PR da tarefa.

## Domínio Go — issue #28

A IA auxiliou na validação EAN/UPC, definição da porta de consulta, erros
nomeados, propagação de contexto e testes sem rede. A regra não importa HTTP,
gRPC ou persistência. Resultados de teste e limites ficam no PR da tarefa;
isso não comprova a implementação dos adaptadores.
