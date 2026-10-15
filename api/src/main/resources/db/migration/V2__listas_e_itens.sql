CREATE TABLE lista (
    id UUID PRIMARY KEY,
    dono_id UUID NOT NULL,
    nome VARCHAR(120) NOT NULL,
    nome_normalizado VARCHAR(240) NOT NULL,
    criada_em TIMESTAMPTZ NOT NULL,
    atualizada_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_lista_dono_nome UNIQUE (dono_id, nome_normalizado),
    CONSTRAINT chk_lista_nome CHECK (length(trim(nome_normalizado)) > 0)
);

CREATE INDEX idx_lista_dono_atualizacao ON lista(dono_id, atualizada_em DESC, id);

CREATE TABLE item (
    id UUID PRIMARY KEY,
    lista_id UUID NOT NULL REFERENCES lista(id) ON DELETE CASCADE,
    nome VARCHAR(120) NOT NULL,
    nome_normalizado VARCHAR(240) NOT NULL,
    quantidade NUMERIC(12, 3),
    unidade VARCHAR(30),
    categoria VARCHAR(60),
    preco_estimado NUMERIC(11, 2),
    CONSTRAINT chk_item_nome CHECK (length(trim(nome_normalizado)) > 0),
    CONSTRAINT chk_item_quantidade CHECK (quantidade IS NULL OR quantidade > 0),
    CONSTRAINT chk_item_preco CHECK (preco_estimado IS NULL OR preco_estimado >= 0)
);

CREATE INDEX idx_item_lista_nome ON item(lista_id, nome_normalizado, id);
CREATE INDEX idx_item_lista_categoria ON item(lista_id, categoria);
