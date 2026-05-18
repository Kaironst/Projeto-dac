# Checkpoint da issue #36 - R1 Autocadastro

Data do checkpoint: 2026-05-18

## Contexto

Este documento registra o estado da implementacao da issue #36 quando o trabalho foi interrompido antes da validacao final ponta a ponta. A implementacao de integracao foi iniciada e boa parte ja esta no workspace, mas o R1 ainda nao deve ser considerado fechado enquanto os itens de correcao e validacao abaixo nao forem concluidos.

## Ja implementado no workspace

- Criado `authService` com MongoDB, BCrypt, criacao/rollback de usuario por SAGA e login via RabbitMQ.
- Criados contratos compartilhados de autenticacao em `AuthDto`.
- Adicionada operacao `LOGIN` em `MessageOperations`.
- Integrado o orquestrador da SAGA para, apos criar a conta, criar o usuario de autenticacao antes de enviar o e-mail de aprovacao.
- Adicionada compensacao de Auth antes do rollback de conta quando a falha ocorre depois da criacao do usuario.
- Adicionado `auth-service` e `auth-database` ao `docker-compose.yml`.
- Adicionado endpoint `POST /login` no API Gateway Node, encaminhando para o `authService`.
- Adicionado proxy `/login` no frontend.
- Login do frontend passou a chamar backend real em vez de simular autenticacao local.
- `mailService` recebeu fallback para simular envio quando credenciais SMTP nao estiverem configuradas, permitindo testes locais sem provedor externo.
- `docs/r1-autocadastro-saga.md` foi atualizado com o fluxo final esperado da SAGA.

## Validacoes ja observadas

- `npm run build` do frontend passou quando executado com rede liberada para baixar Google Fonts.
- O `docker compose config --quiet` passou.
- O build Docker foi iniciado para:
  - `auth-service`
  - `users-service`
  - `contas-service`
  - `gerentes-service`
  - `orchestrator-service`
  - `email-service`
  - `api-gateway-service-node`
- O `api-gateway-service-node` passou pelo `npx tsc`.
- O `auth-service` passou pela etapa de publicacao do modulo compartilhado.
- O build Docker falhou depois, na etapa final `gradle build -x test` do `auth-service`.

Erro observado:

```text
/app/authService/src/main/java/br/ufpr/dac/authService/messaging/SagaConsumer.java:15: error: package tools.jackson.core.type does not exist
import tools.jackson.core.type.TypeReference;

/app/authService/src/main/java/br/ufpr/dac/authService/messaging/SagaConsumer.java:16: error: package tools.jackson.databind does not exist
import tools.jackson.databind.ObjectMapper;
```

## O que ficou faltando

1. Corrigir o build do `auth-service`.

   O primeiro bloqueio conhecido esta em `backend/authService/src/main/java/br/ufpr/dac/authService/messaging/SagaConsumer.java`. Ajustar os imports Jackson para o pacote usado pelo restante do projeto/dependencias disponiveis, provavelmente `com.fasterxml.jackson.core.type.TypeReference` e `com.fasterxml.jackson.databind.ObjectMapper`, ou entao alinhar a dependencia Jackson no `build.gradle.kts`.

2. Concluir o build Docker completo.

   Depois de corrigir o `auth-service`, rodar novamente o build e confirmar se todos os servicos terminam com `BUILD SUCCESSFUL`.

3. Garantir que o e-mail simulado exponha a senha temporaria no ambiente local de teste.

   Para validar login ponta a ponta sem SMTP real, o `mailService` precisa permitir recuperar a senha temporaria gerada. A forma mais simples e registrar o conteudo do e-mail apenas quando SMTP estiver desconfigurado. Sem isso, o usuario e criado no Auth, mas nao ha como saber a senha gerada durante o teste automatizado/manual.

4. Subir o ambiente completo via Docker Compose.

   Servicos minimos para R1:

   - `rabbitmq`
   - `users-database`
   - `contas-database`
   - `gerentes-database`
   - `auth-database`
   - `users-service`
   - `contas-service`
   - `gerentes-service`
   - `auth-service`
   - `email-service`
   - `orchestrator-service`
   - `api-gateway-service-node`

5. Preparar massa de gerente.

   O autocadastro depende de haver gerente disponivel para associacao. Antes do teste, confirmar que existe ao menos um gerente retornando em `GET /gerentes`. Se nao existir, criar um gerente pelo fluxo existente ou inserir uma massa controlada de teste.

6. Validar o fluxo feliz ponta a ponta.

   Roteiro esperado:

   - enviar `POST /clientes` com CPF/e-mail novos;
   - confirmar resposta de solicitacao enviada, sem criar conta imediatamente;
   - confirmar que a solicitacao aparece para o gerente em pedidos pendentes;
   - aprovar a solicitacao pelo endpoint de aprovacao;
   - confirmar que cliente ativo foi criado;
   - confirmar que conta foi criada somente apos aprovacao;
   - recuperar a senha temporaria enviada/simulada por e-mail;
   - executar `POST /login` com o e-mail do cliente aprovado e a senha temporaria;
   - confirmar retorno de token e tipo `CLIENTE`.

7. Validar que pendente nao consegue login.

   Antes da aprovacao, chamar `POST /login` com o e-mail do autocadastro pendente. O resultado esperado e `401`, pois o usuario Auth ainda nao deve existir.

8. Validar CPF duplicado com mensagem clara.

   Testar:

   - CPF ja existente em cliente ativo;
   - CPF ja existente em solicitacao pendente.

   Ambos devem falhar com mensagem clara para o frontend/API Gateway.

9. Validar compensacao.

   Simular uma falha apos a criacao de conta ou apos a criacao de Auth e confirmar:

   - rollback de Auth quando aplicavel;
   - rollback de conta quando aplicavel;
   - solicitacao marcada como `FALHA`;
   - e-mail de falha enviado ou simulado.

10. Rodar checagens finais.

   Comandos recomendados:

   ```bash
   git diff --check
   npm run build
   docker compose config --quiet
   docker compose build auth-service users-service contas-service gerentes-service orchestrator-service email-service api-gateway-service-node
   ```

   Depois disso, executar os testes manuais ou automatizados do roteiro ponta a ponta acima.

11. Fechar a issue #36 somente depois da validacao.

   A issue #36 representa a integracao final do R1. Ela deve ser fechada apenas quando o fluxo feliz, pendente sem login, CPF duplicado e compensacao estiverem validados.

## Observacoes para retomada

- Ha varias alteracoes pendentes no workspace, incluindo mudancas de issues anteriores do R1. Nao usar `git reset` ou checkout destrutivo sem revisar o que e alteracao intencional.
- `docs/project-especs.md` aparece dentro de `docs/`; preservar esse arquivo.
- O build Docker ficou demorado nas etapas Gradle. Ao retomar, pode valer construir primeiro so `auth-service`, `orchestrator-service`, `email-service` e `api-gateway-service-node` para isolar falhas da #36.
- Se o ambiente local nao tiver SMTP, manter a simulacao de e-mail ativa para a validacao de R1.
