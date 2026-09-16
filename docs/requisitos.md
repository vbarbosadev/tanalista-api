# Requisitos iniciais

## Requisitos funcionais

| ID | Prioridade | Requisito | Dependências |
| --- | --- | --- | --- |
| RF-01 | P1 | Criar, consultar, renomear e excluir listas | Conta autenticada |
| RF-02 | P1 | Impedir nomes normalizados duplicados entre listas do mesmo dono | RF-01 |
| RF-03 | P1 | Adicionar, editar, consultar e remover itens de uma lista | RF-01 |
| RF-04 | P1 | Impedir nomes normalizados duplicados dentro da mesma lista | RF-03 |
| RF-05 | P1 | Iniciar uma Compra a partir de uma lista | RF-01 e RF-03 |
| RF-06 | P1 | Registrar preço encontrado e quantidade levada por item | RF-05 |
| RF-07 | P1 | Calcular subtotal por registro e total somente dos itens comprados | RF-06 |
| RF-08 | P1 | Finalizar uma Compra e preservar seu histórico | RF-05 a RF-07 |
| RF-09 | P2 | Compartilhar lista com outra conta usando papel de editor | RF-01 e contas |
| RF-10 | P1 | Processar reenvios offline sem duplicar efeitos | Operações de escrita |
| RF-11 | P2 | Consultar compras finalizadas por lista | RF-08 |

As histórias correspondentes estão no
[GitHub Project do backend](https://github.com/users/vbarbosadev/projects/3). Os recursos
HTTP planejados são detalhados em [`contrato-api.md`](contrato-api.md).

## Requisitos não funcionais

| ID | Categoria | Requisito verificável | Verificação |
| --- | --- | --- | --- |
| RNF-01 | Segurança | Um usuário não acessa lista sem vínculo de acesso | Teste automatizado anti-BOLA retorna `403` ou `404` |
| RNF-02 | Segurança | Segredos não são versionados | Busca no histórico e secret scanning da CI |
| RNF-03 | Consistência | Escritas offline repetidas não duplicam efeitos | Teste envia duas vezes a mesma `Idempotency-Key` |
| RNF-04 | Concorrência | Só uma Compra fica em andamento por lista | Teste concorrente tenta abrir duas compras |
| RNF-05 | Desempenho | Consultas paginadas comuns respondem em até 500 ms no ambiente de demonstração | Teste com base contendo ao menos mil itens |
| RNF-06 | Disponibilidade | API e serviços expõem health check | Verificação automatizada na implantação |
| RNF-07 | Portabilidade | Build e testes rodam por tasks do `mise` em ambiente limpo | `mise run ci` local e no GitHub Actions |
| RNF-08 | Persistência | Alterações de esquema usam apenas migrações versionadas | CI inicia banco vazio e aplica Flyway |
| RNF-09 | Observabilidade | Requisições produzem logs estruturados com correlação | Teste ou demonstração inspeciona os logs JSON |
| RNF-10 | Compatibilidade | Contrato publicado permanece compatível com o aplicativo | OpenAPI validado e teste de contrato |

## Restrições

- Serviço principal em Java 21 com Quarkus.
- PostgreSQL como banco relacional.
- Serviço auxiliar obrigatório em Go, com responsabilidade específica ainda em definição.
- Comunicação entre serviços por gRPC e Protocol Buffers a partir da Sprint 2.
- Android é o cliente prioritário; o backend não depende da plataforma do cliente.
