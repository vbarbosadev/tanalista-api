-- Não modificar V2 aplicada: a garantia concorrente é adicionada nesta versão.
ALTER TABLE item ADD CONSTRAINT uq_item_lista_nome UNIQUE (lista_id, nome_normalizado);
