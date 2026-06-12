package br.ufpr.dac.shared.keys;

public class MessageOperations {

  // Operações de crud
  public static final String CREATE = "CREATE";
  public static final String READ = "READ";
  public static final String READ_ALL = "READ_ALL";
  public static final String UPDATE = "UPDATE";
  public static final String DELETE = "DELETE";

  public static final String READ_BY_EMAIL = "READ_BY_EMAIL";
  public static final String READ_BY_CPF = "READ_BY_CPF";
  public static final String DEPOSITO = "DEPOSITO";
  public static final String SAQUE = "SAQUE";
  public static final String TRANSFERENCIA = "TRANSFERENCIA";

  // --------------------------------------------------------------------------------------------------------

  // Resultados de operações
  public static final String RESULT = "RESULT";
  public static final String ERROR_GENERIC = "ERROR";
  public static final String ERROR_CPF_DUPLICADO = "ERROR_CPF_DUPLICADO";
  public static final String ERROR_INVALID_LOGIN = "ERROR_NO_LOGIN";

  // --------------------------------------------------------------------------------------------------------

  // Operações de login
  public static final String LOGIN = "LOGIN";
  public static final String LOGOUT = "LOGOUT";

  // --------------------------------------------------------------------------------------------------------

  // Operações e resultados de saga
  public static class SagaOperations {

    public static class InsertGerente {
      // InsertGerente
      public static final String START = "INSERT_GERENTE_START_INSERIR_GERENTE";
      public static final String GET_COM_MAIS_CONTAS = "INSERT_GERENTE_GET_COM_MAIS_CONTAS";
      public static final String INSERIR_NOVO = "INSERT_GERENTE_INSERT_NOVO";
      public static final String MOVER_CONTAS = "INSERT_GERENTE_MOVER_CONTAS";
      public static final String ROLLBACK_REMOVER_GERENTE = "INSERT_GERENTE_ROLLBACK_REMOVER_GERENTE";
      // InsertGerente - Resultado de operações
      public static final String GET_COM_MAIS_CONTAS_RESULT = "INSERT_GERENTE_GET_COM_MAIS_CONTAS_RESULT";
      public static final String GET_COM_MAIS_CONTAS_ERROR = "INSERT_GERENTE_GET_COM_MAIS_CONTAS_ERROR";
      public static final String INSERIR_NOVO_RESULT = "INSERT_GERENTE_INSERT_NOVO_RESULT";
      public static final String INSERIR_NOVO_ERROR = "INSERT_GERENTE_INSERT_NOVO_ERROR";
      public static final String MOVER_CONTAS_RESULT = "INSERT_GERENTE_MOVER_CONTAS_RESULT";
      public static final String MOVER_CONTAS_ERROR = "INSERT_GERENTE_MOVER_CONTAS_ERROR";
      public static final String ROLLBACK_REMOVER_GERENTE_RESULT = "INSERT_GERENTE_ROLLBACK_REMOVER_GERENTE_RESULT";
      public static final String ROLLBACK_REMOVER_GERENTE_ERROR = "INSERT_GERENTE_ROLLBACK_REMOVER_GERENTE_ERROR";
    }

    public static class RemoveGerente {
      // RemoveGerente
      public static final String START = "REMOVE_GERENTE_START_REMOVER_GERENTE";
      public static final String GET_TODOS_GERENTES = "REMOVE_GERENTE_GET_TODOS_GERENTES";
      public static final String GET_COM_MENOS_CONTAS = "REMOVE_GERENTE_GET_COM_MENOS_CONTAS";
      public static final String MOVER_CONTAS = "REMOVE_GERENTE_MOVER_CONTAS";
      public static final String REMOVER_GERENTE = "REMOVE_GERENTE_REMOVER_GERENTE";
      public static final String ROLLBACK_REVERTER_MOVER_CONTAS = "REMOVE_GERENTE_ROLLBACK_REVERTER_MOVE";
      // RemoveGerente - Resultado de operações
      public static final String GET_TODOS_GERENTES_RESULT = "REMOVE_GERENTE_GET_TODOS_GERENTES_RESULT";
      public static final String GET_TODOS_GERENTES_ERROR = "REMOVE_GERENTE_GET_TODOS_GERENTES_ERROR";
      public static final String GET_COM_MENOS_CONTAS_RESULT = "REMOVE_GERENTE_GET_COM_MENOS_CONTAS_RESULT";
      public static final String GET_COM_MENOS_CONTAS_ERROR = "REMOVE_GERENTE_GET_COM_MENOS_CONTAS_ERROR";
      public static final String MOVER_CONTAS_RESULT = "REMOVE_GERENTE_MOVER_CONTAS_RESULT";
      public static final String MOVER_CONTAS_ERROR = "REMOVE_GERENTE_MOVER_CONTAS_ERROR";
      public static final String REMOVER_GERENTE_RESULT = "REMOVE_GERENTE_REMOVER_GERENTE_RESULT";
      public static final String REMOVER_GERENTE_ERROR = "REMOVE_GERENTE_REMOVER_GERENTE_ERROR";
      public static final String ROLLBACK_REVERTER_MOVER_CONTAS_RESULT = "REMOVE_GERENTE_ROLLBACK_REVERTER_MOVE_RESULT";
      public static final String ROLLBACK_REVERTER_MOVER_CONTAS_ERROR = "REMOVE_GERENTE_ROLLBACK_REVERTER_MOVE_ERROR";

    }

  }

}
