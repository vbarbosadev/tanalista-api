# Proposta - TáNaLista Backend

> DIM0547 - Desenvolvimento de Sistemas Web II | Sprint 0 | 2026.2

## 1. Visão do produto

```text
Para quem faz a compra da casa
Que usa papel, bloco de notas ou mensagens e só descobre o total no caixa
O TáNaLista é um aplicativo de listas de compras apoiado por uma API colaborativa
Que mantém listas compartilhadas, sincroniza alterações e registra o total real da compra
Diferente de aplicativos que armazenam apenas nomes de produtos em um único aparelho
Nosso produto separa planejamento de compra, preserva o histórico e funciona mesmo com conexão instável
```

**Hipótese de valor.** Acreditamos que pessoas que fazem a compra da casa usarão o
TáNaLista durante o planejamento e no corredor do mercado porque poderão compartilhar
a mesma lista, registrar preços encontrados e conhecer o total antes de chegar ao caixa,
sem perder as alterações realizadas offline.

O backend é o ponto de consistência dos dados compartilhados. Ele aplica as regras de
unicidade, autorização, idempotência e fechamento de compra entre aparelhos diferentes.

## 2. Definição do MVP

| No MVP | Fora do MVP |
| --- | --- |
| Conta de usuário e lista compartilhada com papéis de dono e editor | Catálogo próprio de produtos e preços de mercado |
| Criar, consultar, renomear e excluir listas | Comparação de preços entre estabelecimentos |
| Itens com quantidade, unidade, categoria e preço estimado | Sugestão automática de produtos recorrentes |
| Nome normalizado único para listas e itens | Orçamento máximo com alerta |
| Iniciar e finalizar uma compra no Modo Mercado | Edição simultânea em tempo real |
| Registrar preço encontrado, quantidade levada e subtotal | Notificações |
| Total composto somente pelos itens efetivamente comprados | Estatísticas e painéis de gasto |
| Sincronização idempotente das alterações realizadas offline | Integração com e-commerce ou pagamento |
| Histórico de compras finalizadas | iOS como plataforma prioritária |

**Critérios de sucesso.** Uma compra mensal deve ser registrada de ponta a ponta, com
total conferindo com o cupom dentro da margem dos itens não planejados; duas pessoas
devem conseguir usar uma lista compartilhada; e as operações acumuladas offline não
podem gerar registros duplicados ao sincronizar.

## 3. Backlog inicial

O backlog do backend é mantido no
[GitHub Project TáNaLista API - Backlog](https://github.com/users/vbarbosadev/projects/3).
As histórias são registradas como issues, priorizadas em P1, P2 ou P3, estimadas em
pontos de história na escala de Fibonacci e associadas à sprint prevista.

| Prio | História | Est. | Sprint |
| --- | --- | ---: | ---: |
| P1 | Como usuário, quero criar uma lista para organizar uma ocasião de compra | 3 | 1 |
| P1 | Como usuário, quero consultar minhas listas com o progresso para saber o que falta | 3 | 1 |
| P1 | Como usuário, quero adicionar itens com quantidade, unidade e categoria para planejar a compra | 5 | 1 |
| P1 | Como usuário, quero impedir itens equivalentes na mesma lista para evitar duplicação | 3 | 1 |
| P1 | Como usuário, quero editar e remover itens para corrigir o planejamento | 2 | 1 |
| P1 | Como usuário, quero iniciar uma compra para executar a lista no mercado | 5 | 2 |
| P1 | Como usuário, quero registrar preço e quantidade comprada para acompanhar o total | 5 | 2 |
| P1 | Como usuário, quero consultar um produto pelo código de barras para adicionar o item com menos digitação | 5 | 2 |
| P2 | Como usuário, quero compartilhar uma lista para organizar a compra com outra pessoa | 5 | Final |

## 4. Entidades principais do domínio

```text
Usuário  --< AcessoLista >-- Lista       papel: dono | editor
Lista    --< Item                         nome normalizado único por lista
Lista    --< Compra                       sessão do Modo Mercado
Compra   --< RegistroCompra >-- Item      preço encontrado e quantidade levada
```

| Entidade | Responsabilidade |
| --- | --- |
| **Usuário** | Representa a conta que cria e compartilha listas |
| **Lista** | Planejamento reutilizável de uma ocasião de compra |
| **AcessoLista** | Relaciona usuário e lista com papel de dono ou editor |
| **Item** | Item planejado, com quantidade, unidade, categoria e preço estimado |
| **Compra** | Execução de uma lista no Modo Mercado, com início, fim e estado |
| **RegistroCompra** | Fotografia do item comprado, com preço encontrado, quantidade e subtotal |

Cada lista possui exatamente um dono e pode ter vários editores. O nome normalizado da
lista é único para seu dono, e o nome normalizado do item é único dentro da lista. A
normalização usa caixa baixa, remove acentos e colapsa espaços; assim, `Arroz` e `arroz`
são equivalentes, mas `Leite 200g` e `Leite 400g` continuam distintos.

A Compra separa planejamento de execução para que uma lista possa ser reutilizada. Só
pode existir uma compra em andamento por lista. O RegistroCompra preserva os dados
necessários ao histórico mesmo se o item planejado for alterado depois. Operações vindas
da fila offline devem carregar uma chave idempotente para que reenvios não dupliquem
alterações.

## 5. Decisão da stack principal

Escolhemos **Java 21 com Quarkus** porque o responsável pelo backend possui conhecimento
prévio de Java e experiência com Spring. Injeção de dependência, APIs REST, persistência
com ORM, configuração por ambiente e organização em camadas têm conceitos semelhantes,
o que reduz a curva de aprendizado e permite concentrar o trabalho nas decisões de
arquitetura e nas regras do produto.

Quarkus também oferece inicialização rápida, baixo consumo em containers e integração
com Hibernate, Flyway, PostgreSQL, OpenAPI e JUnit. Kotlin/Ktor foi considerado, mas foi
descartado nesta entrega porque adicionaria simultaneamente uma nova linguagem e um novo
framework dentro do prazo da disciplina.

## 6. Divisão entre o serviço principal e Go

| Java/Quarkus | Serviço Go |
| --- | --- |
| Entidades, regras de domínio e persistência | Consulta concorrente a fontes externas por código de barras |
| Autenticação e autorização por recurso | Validação de códigos EAN/UPC e normalização de metadados |
| CRUD de listas, itens e compras | Timeout, cancelamento e tratamento de falhas dos provedores |
| Criação do Item após confirmação do usuário | Cache de curta duração e métricas a partir da Sprint 3 |

O primeiro microsserviço Go será o **barcode-lookup-service**. O aplicativo envia o
código de barras para a API Quarkus, que chama o Go por gRPC com deadline. O serviço Go
valida o código e consulta uma ou mais fontes externas, começando pelo Open Food Facts,
para devolver nome, marca, categoria e unidade normalizados.

A separação é justificada pelo trabalho de I/O externo, pelo uso de concorrência entre
provedores e pelo controle de cancelamento com `context`. O Go não mantém catálogo
próprio, não acessa as tabelas centrais e não cria itens. O Quarkus continua responsável
por autorização, persistência e regras do domínio após o usuário confirmar os dados.

## 7. Equipe

| Nome | Matrícula | Conta GitHub | Papel em DIM0547 |
| --- | --- | --- | --- |
| Vinicius Barbosa | 20230051760 | vbarbosadev | Desenvolvedor principal e responsável pelo backend |
| Thallys | 20240011552 | thallystorres | Apoio na validação do domínio e dos critérios de aceitação |
| Ivis | 20220028454 | ivixs | Apoio na organização do backlog e do processo |

## 8. Coorte e integração

**Coorte de apresentação:** B - online.

O aplicativo cliente é desenvolvido em **DIM0524 - Desenvolvimento de Sistemas para
Dispositivos Móveis** e consome esta API. O mesmo produto é usado em **DIM0510 -
Processos de Software**, com entregáveis próprios. Em DIM0547, o objeto avaliado é o
backend, seus contratos e sua infraestrutura.
