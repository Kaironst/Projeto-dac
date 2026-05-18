package br.ufpr.dac.shared.keys;

public class MessageOperations {

  // Operações de crud
  public static final String CREATE = "CREATE";
  public static final String READ = "READ";
  public static final String READ_ALL = "READ_ALL";
  public static final String UPDATE = "UPDATE";
  public static final String DELETE = "DELETE";
  public static final String LOGIN = "LOGIN";
  public static final String VALIDATE_AUTOCADASTRO_CPF = "VALIDATE_AUTOCADASTRO_CPF";
  public static final String READ_AUTOCADASTRO_PENDENTES = "READ_AUTOCADASTRO_PENDENTES";

  // --------------------------------------------------------------------------------------------------------

  // Resultados de operações
  public static final String RESULT = "RESULT";
  public static final String ERROR_GENERIC = "ERROR";
  public static final String ERROR_CPF_DUPLICADO = "ERROR_CPF_DUPLICADO";

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
      public static final String GET_COM_MENOS_CONTAS = "REMOVE_GERENTE_GET_COM_MENOS_CONTAS";
      public static final String MOVER_CONTAS = "REMOVE_GERENTE_MOVER_CONTAS";
      public static final String REMOVER_GERENTE = "REMOVE_GERENTE_REMOVER_GERENTE";
      public static final String ROLLBACK_REVERTER_MOVER_CONTAS = "REMOVE_GERENTE_ROLLBACK_REVERTER_MOVE";
      // RemoveGerente - Resultado de operações
      public static final String GET_COM_MENOS_CONTAS_RESULT = "REMOVE_GERENTE_GET_COM_MENOS_CONTAS_RESULT";
      public static final String GET_COM_MENOS_CONTAS_ERROR = "REMOVE_GERENTE_GET_COM_MENOS_CONTAS_ERROR";
      public static final String MOVER_CONTAS_RESULT = "REMOVE_GERENTE_MOVER_CONTAS_RESULT";
      public static final String MOVER_CONTAS_ERROR = "REMOVE_GERENTE_MOVER_CONTAS_ERROR";
      public static final String REMOVER_GERENTE_RESULT = "REMOVE_GERENTE_REMOVER_GERENTE_RESULT";
      public static final String REMOVER_GERENTE_ERROR = "REMOVE_GERENTE_REMOVER_GERENTE_ERROR";
      public static final String ROLLBACK_REVERTER_MOVER_CONTAS_RESULT = "REMOVE_GERENTE_ROLLBACK_REVERTER_MOVE_RESULT";
      public static final String ROLLBACK_REVERTER_MOVER_CONTAS_ERROR = "REMOVE_GERENTE_ROLLBACK_REVERTER_MOVE_ERROR";

    }

    public static class Autocadastro {
      // Autocadastro
      public static final String START = "AUTOCADASTRO_START";
      public static final String VALIDAR_CPF = "AUTOCADASTRO_VALIDAR_CPF";
      public static final String REGISTRAR_SOLICITACAO = "AUTOCADASTRO_REGISTRAR_SOLICITACAO";
      public static final String ESCOLHER_GERENTE = "AUTOCADASTRO_ESCOLHER_GERENTE";
      public static final String VINCULAR_GERENTE = "AUTOCADASTRO_VINCULAR_GERENTE";
      public static final String APROVAR_SOLICITACAO = "AUTOCADASTRO_APROVAR_SOLICITACAO";
      public static final String REJEITAR_SOLICITACAO = "AUTOCADASTRO_REJEITAR_SOLICITACAO";
      public static final String CRIAR_CONTA = "AUTOCADASTRO_CRIAR_CONTA";
      public static final String CRIAR_AUTH = "AUTOCADASTRO_CRIAR_AUTH";
      public static final String ENVIAR_EMAIL_APROVACAO = "AUTOCADASTRO_ENVIAR_EMAIL_APROVACAO";
      public static final String ENVIAR_EMAIL_REJEICAO = "AUTOCADASTRO_ENVIAR_EMAIL_REJEICAO";
      public static final String ENVIAR_EMAIL_FALHA = "AUTOCADASTRO_ENVIAR_EMAIL_FALHA";
      public static final String ROLLBACK_SOLICITACAO = "AUTOCADASTRO_ROLLBACK_SOLICITACAO";
      public static final String ROLLBACK_CONTA = "AUTOCADASTRO_ROLLBACK_CONTA";
      public static final String ROLLBACK_AUTH = "AUTOCADASTRO_ROLLBACK_AUTH";

      // Autocadastro - resultados
      public static final String VALIDAR_CPF_RESULT = "AUTOCADASTRO_VALIDAR_CPF_RESULT";
      public static final String VALIDAR_CPF_ERROR = "AUTOCADASTRO_VALIDAR_CPF_ERROR";
      public static final String REGISTRAR_SOLICITACAO_RESULT = "AUTOCADASTRO_REGISTRAR_SOLICITACAO_RESULT";
      public static final String REGISTRAR_SOLICITACAO_ERROR = "AUTOCADASTRO_REGISTRAR_SOLICITACAO_ERROR";
      public static final String ESCOLHER_GERENTE_RESULT = "AUTOCADASTRO_ESCOLHER_GERENTE_RESULT";
      public static final String ESCOLHER_GERENTE_ERROR = "AUTOCADASTRO_ESCOLHER_GERENTE_ERROR";
      public static final String VINCULAR_GERENTE_RESULT = "AUTOCADASTRO_VINCULAR_GERENTE_RESULT";
      public static final String VINCULAR_GERENTE_ERROR = "AUTOCADASTRO_VINCULAR_GERENTE_ERROR";
      public static final String APROVAR_SOLICITACAO_RESULT = "AUTOCADASTRO_APROVAR_SOLICITACAO_RESULT";
      public static final String APROVAR_SOLICITACAO_ERROR = "AUTOCADASTRO_APROVAR_SOLICITACAO_ERROR";
      public static final String REJEITAR_SOLICITACAO_RESULT = "AUTOCADASTRO_REJEITAR_SOLICITACAO_RESULT";
      public static final String REJEITAR_SOLICITACAO_ERROR = "AUTOCADASTRO_REJEITAR_SOLICITACAO_ERROR";
      public static final String CRIAR_CONTA_RESULT = "AUTOCADASTRO_CRIAR_CONTA_RESULT";
      public static final String CRIAR_CONTA_ERROR = "AUTOCADASTRO_CRIAR_CONTA_ERROR";
      public static final String CRIAR_AUTH_RESULT = "AUTOCADASTRO_CRIAR_AUTH_RESULT";
      public static final String CRIAR_AUTH_ERROR = "AUTOCADASTRO_CRIAR_AUTH_ERROR";
      public static final String ENVIAR_EMAIL_APROVACAO_RESULT = "AUTOCADASTRO_ENVIAR_EMAIL_APROVACAO_RESULT";
      public static final String ENVIAR_EMAIL_APROVACAO_ERROR = "AUTOCADASTRO_ENVIAR_EMAIL_APROVACAO_ERROR";
      public static final String ENVIAR_EMAIL_REJEICAO_RESULT = "AUTOCADASTRO_ENVIAR_EMAIL_REJEICAO_RESULT";
      public static final String ENVIAR_EMAIL_REJEICAO_ERROR = "AUTOCADASTRO_ENVIAR_EMAIL_REJEICAO_ERROR";
      public static final String ENVIAR_EMAIL_FALHA_RESULT = "AUTOCADASTRO_ENVIAR_EMAIL_FALHA_RESULT";
      public static final String ENVIAR_EMAIL_FALHA_ERROR = "AUTOCADASTRO_ENVIAR_EMAIL_FALHA_ERROR";
      public static final String ROLLBACK_SOLICITACAO_RESULT = "AUTOCADASTRO_ROLLBACK_SOLICITACAO_RESULT";
      public static final String ROLLBACK_SOLICITACAO_ERROR = "AUTOCADASTRO_ROLLBACK_SOLICITACAO_ERROR";
      public static final String ROLLBACK_CONTA_RESULT = "AUTOCADASTRO_ROLLBACK_CONTA_RESULT";
      public static final String ROLLBACK_CONTA_ERROR = "AUTOCADASTRO_ROLLBACK_CONTA_ERROR";
      public static final String ROLLBACK_AUTH_RESULT = "AUTOCADASTRO_ROLLBACK_AUTH_RESULT";
      public static final String ROLLBACK_AUTH_ERROR = "AUTOCADASTRO_ROLLBACK_AUTH_ERROR";
    }

  }

}
