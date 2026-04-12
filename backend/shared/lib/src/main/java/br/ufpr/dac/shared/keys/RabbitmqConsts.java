package br.ufpr.dac.shared.keys;

public class RabbitmqConsts {

  // exchange padrão
  public static final String APP_EXCHANGE = "app.exchange";

  // queue do orquestratorService
  public static final String ORCHESTRATOR_QUEUE = "orchestrator.queue";
  // key do orquestratorService
  public static final String ORCHESTRATOR_KEY = "orchestrator.key";

  // queue do usersService
  public static final String USERS_QUEUE = "users.queue";
  // key do usersService
  public static final String USERS_KEY = "users.key";

  // queue do contasService
  public static final String CONTAS_QUEUE = "contas.queue";
  // key do contas service
  public static final String CONTAS_KEY = "contas.key";

  // key do apiGateway (queue em node apenas)
  public static final String API_GATEWAY_KEY = "apiGateway.key";

}
