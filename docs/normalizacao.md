# Normalização de nomes

Lista por dono e Item por lista usam `Nomes.normalizar` antes de persistir:
decomposição Unicode NFD, remoção de marcas de acento, espaços Unicode repetidos
reduzidos a um, remoção de espaços nas pontas e caixa baixa com `Locale.ROOT`.

`"  CAFÉ   com Leite "` e `"cafe com leite"` conflitam; `Leite 200g` e `Leite 400g`
permanecem distintos. A normalização não tenta inferir unidades ou produtos.

Os adaptadores verificam conflitos antes da escrita, sem substituir a restrição
única do PostgreSQL. V2 garante `(dono_id, nome_normalizado)` em Lista; V3 garante
`(lista_id, nome_normalizado)` em Item, inclusive para escritas concorrentes.
O `flush` dentro da transação traduz somente essas restrições conhecidas para
erro de conflito do domínio. O adaptador HTTP o apresenta como `409`.

Na edição, o próprio ID é excluído da verificação; renomear sem alterar o nome
normalizado é permitido. Filtros por nome usam a mesma normalização e busca por
substring literal, sem interpretar `%` e `_` como curingas SQL.
