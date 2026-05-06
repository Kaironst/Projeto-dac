package br.ufpr.dac.shared.keys;

public class RabbitmqConsts {

  // exchange padrão
  public static final String APP_EXCHANGE = "app.exchange";

  // --------------------------------------------------------------------------------------------------------

  // key para o listener de clientes
  public static final String ORCHESTRATOR_USERS_KEY = "orchestrator.users.key";
  // queue para o listener de clientes
  public static final String ORCHESTRATOR_USERS_QUEUE = "orchestrator.users.queue";

  // key para o listener de gerentes
  public static final String ORCHESTRATOR_GERENTES_KEY = "orchestrator.gerentes.key";
  // queue para o listener de gerentes
  public static final String ORCHESTRATOR_GERENTES_QUEUE = "orchestrator.gerentes.queue";

  // key para o listener de saga
  public static final String ORCHESTRATOR_SAGA_KEY = "orchestrator.saga.key";
  // queue para o listener de saga
  public static final String ORCHESTRATOR_SAGA_QUEUE = "orchestrator.saga.queue";

  // --------------------------------------------------------------------------------------------------------

  // queue do usersService
  public static final String USERS_QUEUE = "users.queue";
  // key do usersService
  public static final String USERS_KEY = "users.key";
  // queue do usersService para sagas
  public static final String USERS_SAGA_QUEUE = "saga.users.queue";
  // key do usersService para sagas
  public static final String USERS_SAGA_KEY = "saga.users.key";

  // --------------------------------------------------------------------------------------------------------

  // queue do gerentesService
  public static final String GERENTES_QUEUE = "gerentes.queue";
  // key do gerentesService
  public static final String GERENTES_KEY = "gerentes.key";
  // queue do gerentesService para sagas
  public static final String GERENTES_SAGA_QUEUE = "saga.gerentes.queue";
  // key do gerentesService para sagas
  public static final String GERENTES_SAGA_KEY = "saga.gerentes.key";

  // --------------------------------------------------------------------------------------------------------

  // queue do contasService
  public static final String CONTAS_QUEUE = "contas.queue";
  // key do contas service
  public static final String CONTAS_KEY = "contas.key";
  // queue do contasService para sagas
  public static final String CONTAS_SAGA_QUEUE = "saga.contas.queue";
  // key do contasService para sagas
  public static final String CONTAS_SAGA_KEY = "saga.contas.key";

  // --------------------------------------------------------------------------------------------------------

  // key do apiGateway (queue em node apenas)
  public static final String API_GATEWAY_KEY = "apiGateway.key";

}
