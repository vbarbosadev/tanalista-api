# Domínio e persistência — Sprint 1

## Implementado nesta sprint

```mermaid
erDiagram
    LISTA ||--o{ ITEM : contem
    LISTA {
        uuid id PK
        uuid dono_id "identidade de desenvolvimento, não FK nesta sprint"
        varchar nome
        varchar nome_normalizado "único por dono"
        timestamptz criada_em
        timestamptz atualizada_em
    }
    ITEM {
        uuid id PK
        uuid lista_id FK
        varchar nome
        varchar nome_normalizado "único por lista"
        numeric quantidade "opcional, positiva"
        varchar unidade "opcional"
        varchar categoria "opcional"
        numeric preco_estimado "opcional, não negativo"
    }
```

Lista tem exatamente um dono, representado por UUID. A Sprint 1 usa uma identidade
fixa de desenvolvimento configurada **no servidor**, não recebida do cliente.
Isso não implementa autenticação. O modo é desabilitado fora do perfil `dev`, a
menos que seja explicitamente habilitado em ambiente confiável.

Migrações novas criam `lista` e `item`, sem alterar `V1__initProdutos.sql`.
`ON DELETE CASCADE` remove o planejamento da lista. A entidade JPA `Produto` e
sua tabela são legado: permanecem intactas e não representam um catálogo novo.
O CRUD novo usa UUID e `/api/v1/listas`; `/produtos` permanece fora do contrato MVP.

## Modelo planejado (não implementado na Sprint 1)

```mermaid
erDiagram
    USUARIO ||--o{ ACESSO_LISTA : possui
    LISTA ||--|{ ACESSO_LISTA : permite
    LISTA ||--o{ ITEM : planeja
    LISTA ||--o{ COMPRA : origina
    USUARIO ||--o{ COMPRA : inicia
    COMPRA ||--o{ REGISTRO_COMPRA : preserva
    ITEM o|--o{ REGISTRO_COMPRA : referencia
    USUARIO {
        uuid id PK
        varchar nome
        varchar email
    }
    ACESSO_LISTA {
        uuid lista_id FK
        uuid usuario_id FK
        varchar papel "DONO ou EDITOR"
    }
    COMPRA {
        uuid id PK
        uuid lista_id FK
        uuid responsavel_id FK
        varchar estado "EM_ANDAMENTO ou FINALIZADA"
        timestamptz aberta_em
        timestamptz finalizada_em
    }
    REGISTRO_COMPRA {
        uuid id PK
        uuid compra_id FK
        uuid item_id FK "opcional após remoção do planejamento"
        varchar nome_item_snapshot
        varchar unidade_snapshot
        numeric preco_encontrado
        numeric quantidade_levada
    }
```

Regras futuras: um acesso DONO por lista; somente uma Compra EM_ANDAMENTO por
lista; RegistroCompra preserva nome/unidade, preço e quantidade históricos sem
depender da existência ou dos valores atuais de Item. A exclusão do planejamento
não deve apagar compras finalizadas. Quando compras forem implementadas, a
política de exclusão da lista deverá evoluir para preservar esse histórico.

Autenticação e compartilhamento substituirão a identidade de desenvolvimento.
Não existem tabelas de usuário, acesso ou compra nesta sprint: não apresentar
esses relacionamentos como persistidos, nem retornar progresso de compra fictício.
