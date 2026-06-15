package br.ufpr.dac.shared.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message wrapper genérico torna mais fácil adicionar propriedades
 * às menssagens
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageWrapper<T> {
  private String operation;
  private List<T> data;
  private String dataType;

  public MessageWrapper(String operation, List<T> data) {
    this.operation = operation;
    this.data = data;
  }

  public static class DataTypes {
    public static final String cliente = "cliente";
    public static final String endereco = "endereco";
    public static final String gerente = "gerente";
    public static final String conta = "conta";
    public static final String itemHistorico = "itemHistorico";
    public static final String email = "email";
  }

}
