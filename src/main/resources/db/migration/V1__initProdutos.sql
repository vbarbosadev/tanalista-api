CREATE TABLE produto (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL,
    unidade_medida VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_status CHECK (status IN ('ATIVO', 'INATIVO', 'SEM_ESTOQUE')),

    CONSTRAINT chk_preco CHECK (preco >= 0),

    CONSTRAINT chk_unidade_medida CHECK (unidade_medida IN ('UN', 'KG', 'L', 'M', 'CM', 'MM', 'G'))

);

CREATE INDEX idx_produto_status ON produto(status);
CREATE INDEX idx_produto_unidade_medida ON produto(unidade_medida);
