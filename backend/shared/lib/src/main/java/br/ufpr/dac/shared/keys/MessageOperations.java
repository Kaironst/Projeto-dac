package br.ufpr.dac.shared.keys;

public class MessageOperations {

  // Operações de crud
  public static final String CREATE = "CREATE";
  public static final String READ = "READ";
  public static final String READ_ALL = "READ_ALL";
  public static final String UPDATE = "UPDATE";
  public static final String DELETE = "DELETE";

  // --------------------------------------------------------------------------------------------------------

  // Operações de saga

  // InsertGerente
  public static final String GET_GERENTES_C_MAIS_CONTAS = "GET_GERENTES_COM_MAIS_CONTAS";

  // --------------------------------------------------------------------------------------------------------

  // Resultados de operações
  public static final String RESULT = "RESULT";
  public static final String ERROR_GENERIC = "ERROR";

}
