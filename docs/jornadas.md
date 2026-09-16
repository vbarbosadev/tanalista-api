# Usuários e jornadas

## Perfis

| Perfil | Objetivo | Permissões principais |
| --- | --- | --- |
| Dono da lista | Planejar e coordenar uma compra doméstica | Criar, editar, compartilhar e excluir a lista; iniciar e finalizar compras |
| Editor da lista | Colaborar no planejamento e na execução | Consultar a lista, alterar itens e registrar itens comprados |

Uma mesma pessoa pode ser dona de uma lista e editora de outra. O papel pertence ao
acesso à lista, não à conta global do usuário.

## Jornada 1 - Planejar uma compra

1. O usuário consulta suas listas ou cria uma nova.
2. Informa um nome que seja único entre as listas das quais é dono.
3. Adiciona itens com nome e, opcionalmente, quantidade, unidade, categoria e preço estimado.
4. A API normaliza os nomes e impede itens equivalentes na mesma lista.
5. O usuário revisa o planejamento e pode reutilizá-lo em compras futuras.

**Resultado esperado:** lista persistida, sem duplicidades por nome normalizado e pronta
para iniciar uma compra.

## Jornada 2 - Executar o Modo Mercado

1. Um usuário com acesso inicia uma Compra a partir da lista.
2. Para cada item encontrado, informa o preço e a quantidade efetivamente levada.
3. A API registra o item comprado e recalcula o total.
4. O total considera somente os registros efetivamente comprados.
5. Ao terminar, o usuário finaliza a Compra e o histórico se torna imutável.

**Fluxos alternativos:** somente uma Compra pode estar em andamento por lista; um reenvio
com a mesma chave idempotente devolve o resultado anterior; alterações estruturais que
conflitem com uma compra aberta são rejeitadas.

## Jornada 3 - Compartilhar uma lista

1. O dono informa a conta que receberá acesso.
2. A API cria um acesso com papel de editor.
3. O editor passa a consultar e alterar os itens da lista.
4. Somente o dono pode gerenciar acessos ou excluir a lista.

**Erros principais:** usuário inexistente, compartilhamento duplicado, tentativa de
alteração por usuário sem acesso e tentativa de exclusão por editor.

## Jornada 4 - Sincronizar após uso offline

1. O aplicativo mantém localmente as operações feitas sem conexão.
2. Ao reconectar, envia cada operação com uma chave idempotente.
3. A API aplica operações inéditas e devolve o resultado das já processadas.
4. Conflitos de versão são informados ao cliente sem sobrescrever dados silenciosamente.

**Resultado esperado:** nenhuma operação é aplicada duas vezes e nenhum erro de
autorização ou conflito é ocultado pelo processo de sincronização.
