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

## Provedor HTTP — issue #57

A IA consultou Orbit/documentação do Open Food Facts, implementou o adaptador e
apoiou testes HTTP controlados e execução contra simulação Postman local.
A busca por mock do proprietário não pôde prosseguir sem login; a fixture foi
criada da documentação com dados sintéticos. Não houve consulta de produto real
nem publicação da simulação. Limitações de cobertura, quota e licença estão
documentadas; resultados reais e CI ficam no PR.

## Servidor gRPC — issue #58

A IA apoiou a implementação do adaptador gRPC, composição do processo Go,
health/reflection e encerramento gracioso limitado. Testes com bufconn,
health check do binário e chamadas grpcurl ao processo local foram usados,
com o provedor apontado para fixture HTTP. Isso não comprova resposta de um
provedor real nem implantação pública; evidências e CI ficam no PR.

## Integração Quarkus e arquitetura — issues #27 e #61

A IA apoiou porta de domínio, adaptador gRPC com deadline, endpoint REST,
tradução de erros e testes contra servidor gRPC controlado. Também apoiou
ArchUnit: uma violação temporária com io.grpc.Channel foi detectada e removida,
seguida de execução limpa da suíte. O PostgreSQL do CRUD não foi substituído
por esses testes: novas suítes específicas continuam em #59/#60. Não houve
implantação pública ou autenticação real nesta etapa.

## Ambiente Compose — issue #62

A IA auxiliou nos builds multiestágio de API/Go, geração dentro dos containers,
health checks, configuração local e no smoke REST -> Go -> HTTP controlado.
A máquina de desenvolvimento não possui Docker: validação de build e Compose
depende do Actions, com logs e resultado no PR. Os testes locais de linguagens
e YAML não são apresentados como execução local de containers.
O override HTTP usa dados sintéticos e não foi publicado como serviço.

## Unitários do CRUD — issue #59

A IA auxiliou na escrita de testes JUnit puros de domínio/casos de uso e de
presença de campos no adaptador PATCH, usando repositórios em memória e relógios
fixos. Autorrevisão e resultados reais ficam no PR. Não são testes de banco e
não comprovam persistência PostgreSQL ou autenticação pública.

## Integração PostgreSQL — issue #60

A IA apoiou a criação da suíte com Testcontainers, perfil Quarkus isolado,
migrações Flyway e testes de CRUD, filtros, unicidade, concorrência e
visibilidade. O recurso cria banco descartável e não aceita credenciais de
banco existente. Nesta máquina sem Docker, compilação e testes unitários são
evidência local; execução de banco depende dos resultados do CI/Codespaces
registrados no PR. Não declarar testes de containers locais inexistentes.

## Ambiente Codespaces — issue #79

Após orientação explícita do responsável para executar o projeto no Codespaces,
a IA preparou devcontainer, scripts e validação automatizada do mesmo ambiente
no Actions. Não criou Codespace na conta do usuário, não consumiu sua quota de
Codespaces, não publicou portas e não escolheu provedor de identidade.
A orientação foi registrada nas #64/#65, mantendo a distinção entre ambiente
de desenvolvimento e os requisitos de implantação pública da rubrica.

Em 17/11/2026, o responsável confirmou testes sem erros no workspace de
`develop` em comentário na #79. A IA conferiu esse registro e documentou a
evidência como relato manual, sem inventar hash, logs ou captura.
A ADR 0002 registra a decisão já adotada de Codespaces; autenticação,
implantação pública, licença, teste por terceiro, daily e vídeo não foram
declarados concluídos.
