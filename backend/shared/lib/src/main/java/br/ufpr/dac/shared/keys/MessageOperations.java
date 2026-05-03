package br.ufpr.dac.shared.keys;

public class MessageOperations {

  // Operações de crud
  public static final String CREATE = "CREATE";
  public static final String READ = "READ";
  public static final String READ_ALL = "READ_ALL";
  public static final String UPDATE = "UPDATE";
  public static final String DELETE = "DELETE";

  // --------------------------------------------------------------------------------------------------------

  // Resultados de operações
  public static final String RESULT = "RESULT";
  public static final String ERROR_GENERIC = "ERROR";

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

  }

}
