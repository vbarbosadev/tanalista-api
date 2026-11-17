CREATE TABLE usuario (
    id UUID PRIMARY KEY,
    email_normalizado VARCHAR(254) NOT NULL,
    senha_hash VARCHAR(512) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_usuario_email UNIQUE (email_normalizado),
    CONSTRAINT chk_usuario_email CHECK (email_normalizado = lower(trim(email_normalizado))
        AND length(email_normalizado) > 3)
);
