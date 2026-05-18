# R1 - Contrato da SAGA de Autocadastro

Este documento define o desenho de contratos/modelo para a issue #36. Ele nao fecha o R1 sozinho; serve como base para implementar #25, #26, #27 e #28 sem divergencia de fluxo.

## Objetivo

O autocadastro deve registrar uma solicitacao pendente. A pessoa so vira cliente ativo depois que o gerente aprova a solicitacao. Na aprovacao, a SAGA cria a conta, cria o usuario de autenticacao, envia a senha por e-mail e libera o login.

## Modelo Compartilhado

Contratos Java:

- `AutocadastroDto.SolicitacaoEntrada`: payload inicial do autocadastro.
- `AutocadastroDto.Solicitacao`: solicitacao pendente/avaliada, com cliente, gerente, status e datas.
- `AutocadastroDto.Aprovacao`: comando de aprovacao enviado pelo gerente.
- `AutocadastroDto.Rejeicao`: comando de rejeicao com motivo.
- `AutocadastroDto.ContaCriada`: resultado da criacao de conta.
- `AutocadastroDto.UsuarioAuth`: dados para criacao do usuario de autenticacao.
- `AutocadastroDto.Notificacao`: e-mail de aprovacao, rejeicao ou falha.
- `AutocadastroDto.Falha`: dados de erro para compensacao e notificacao.

Status da solicitacao:

- `PENDENTE`: solicitacao recebida e ainda sem decisao.
- `APROVADO`: gerente aprovou e a SAGA concluiu criacao de conta/Auth/e-mail.
- `REJEITADO`: gerente rejeitou com motivo.
- `FALHA`: houve falha interna e o fluxo foi compensado quando aplicavel.

## Filas e Rotas

Rotas existentes:

- `orchestrator.saga.key`: entrada e retornos para o orquestrador.
- `saga.users.key`: comandos de SAGA para o servico de clientes.
- `saga.gerentes.key`: comandos de SAGA para o servico de gerentes.
- `saga.contas.key`: comandos de SAGA para o servico de contas.

Rotas integradas na fase final:

- `saga.auth.key`: comandos de SAGA para o servico de autenticacao.
- `saga.mail.key`: comandos de SAGA para notificacoes por e-mail via RabbitMQ.
- `auth.key`: login via API Gateway para o servico de autenticacao.

## Fluxo de Solicitacao

1. API Gateway recebe `POST /clientes` ou futuro endpoint especifico de autocadastro.
2. Gateway publica `AUTOCADASTRO_START` para `orchestrator.saga.key` com `AutocadastroDto.SolicitacaoEntrada`.
3. Orquestrador envia `AUTOCADASTRO_VALIDAR_CPF` para `saga.users.key`.
4. Users valida CPF em clientes ativos e solicitacoes pendentes.
5. Orquestrador envia `AUTOCADASTRO_REGISTRAR_SOLICITACAO` para `saga.users.key`.
6. Users persiste solicitacao com status `PENDENTE`.
7. Orquestrador envia `AUTOCADASTRO_ESCOLHER_GERENTE` para `saga.contas.key` ou composicao equivalente.
8. Servico responsavel retorna o gerente com menos clientes.
9. Orquestrador envia `AUTOCADASTRO_VINCULAR_GERENTE` para persistir o gerente responsavel na solicitacao.
10. Gateway retorna confirmacao assincrona para o front: solicitacao enviada.

Regra de desempate da #25: quando mais de um gerente tiver a menor quantidade de clientes, escolher o menor `id` de gerente entre os empatados. Isso deixa o comportamento deterministico e facil de testar.

## Fluxo de Aprovacao

1. Gerente aprova a solicitacao pendente.
2. Gateway publica `AUTOCADASTRO_APROVAR_SOLICITACAO`.
3. Orquestrador envia `AUTOCADASTRO_APROVAR_SOLICITACAO` para `saga.users.key`.
4. Users cria o cliente ativo a partir da solicitacao, marca a solicitacao como `APROVADO` e retorna os IDs de cliente/gerente.
5. Orquestrador envia `AUTOCADASTRO_CRIAR_CONTA` para `saga.contas.key`.
6. Contas gera numero aleatorio de 4 digitos, calcula limite e persiste a conta.
7. Orquestrador gera a senha temporaria e envia `AUTOCADASTRO_CRIAR_AUTH` para `saga.auth.key`.
8. Auth aplica hash BCrypt e persiste usuario do tipo `CLIENTE` no MongoDB.
9. Orquestrador envia `AUTOCADASTRO_ENVIAR_EMAIL_APROVACAO` para `saga.mail.key`.

Estado da #36: a senha enviada por e-mail e a senha persistida no Auth sao a mesma. O login passa pelo gateway em `POST /login`, valida BCrypt no `authService` e retorna token JWT assinado.

## Fluxo de Rejeicao

1. Gerente informa motivo de rejeicao.
2. Gateway publica `AUTOCADASTRO_REJEITAR_SOLICITACAO`.
3. Orquestrador marca a solicitacao como `REJEITADO`, com `motivoRejeicao` e `dataAnalise`.
4. Orquestrador envia `AUTOCADASTRO_ENVIAR_EMAIL_REJEICAO`.

## Compensacao

Falhas antes da criacao de conta/Auth:

- marcar solicitacao como `FALHA`;
- enviar `AUTOCADASTRO_ENVIAR_EMAIL_FALHA`.

Falha depois da criacao de conta:

- enviar `AUTOCADASTRO_ROLLBACK_CONTA`;
- marcar solicitacao como `FALHA`;
- enviar e-mail de falha.

Falha depois da criacao de Auth:

- enviar `AUTOCADASTRO_ROLLBACK_AUTH`;
- enviar `AUTOCADASTRO_ROLLBACK_CONTA`;
- marcar solicitacao como `FALHA`;
- enviar e-mail de falha.

## Relacao com Issues

- #25 implementa `AUTOCADASTRO_ESCOLHER_GERENTE` e persistencia da associacao.
- #26 implementa aprovacao assincrona, `AUTOCADASTRO_CRIAR_CONTA` e garante que conta so exista apos aprovacao.
- #27 implementa notificacoes de aprovacao e falha via `mailService`/RabbitMQ.
- #28 remove `localStorage` do front, usa `/clientes`, `/gerentes` e `/contas` no gateway, e deixa o front como camada de apresentacao.
- #36 integra Auth/MongoDB, login real, compensacao de Auth e validacao ponta a ponta do R1.

## Fora Desta Fase

Ainda fica fora do escopo atual a notificacao formal de rejeicao, que pertence ao R11. O R1 de autocadastro/aprovacao fica integrado.
