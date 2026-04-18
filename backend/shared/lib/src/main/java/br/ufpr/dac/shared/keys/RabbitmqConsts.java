package br.ufpr.dac.shared.keys;

public class RabbitmqConsts {

  // exchange padrão
  public static final String APP_EXCHANGE = "app.exchange";

  // --------------------------------------------------------------------------------------------------------

  // key para o listener de clientes
  public static final String ORCHESTRATOR_USERS_KEY = "orchestrator.users.key";
  // queue para o listener de clientes
  public static final String ORCHESTRATOR_USERS_QUEUE = "orchestrator.users.key";

  // key para o listener de gerentes
  public static final String ORCHESTRATOR_GERENTES_KEY = "orchestrator.gerentes.key";
  // queue para o listener de gerentes
  public static final String ORCHESTRATOR_GERENTES_QUEUE = "orchestrator.gerentes.key";

  // --------------------------------------------------------------------------------------------------------

  // queue do usersService
  public static final String USERS_QUEUE = "users.queue";
  // key do usersService
  public static final String USERS_KEY = "users.key";

  // --------------------------------------------------------------------------------------------------------

  // queue do gerentesService
  public static final String GERENTES_QUEUE = "gerentes.queue";
  // key do gerentesService
  public static final String GERENTES_KEY = "gerentes.key";

  // --------------------------------------------------------------------------------------------------------

  // queue do contasService
  public static final String CONTAS_QUEUE = "contas.queue";
  // key do contas service
  public static final String CONTAS_KEY = "contas.key";

  // --------------------------------------------------------------------------------------------------------

  // key do apiGateway (queue em node apenas)
  public static final String API_GATEWAY_KEY = "apiGateway.key";

}
