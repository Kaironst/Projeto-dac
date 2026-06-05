package br.ufpr.dac.shared.dto.saga;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SagaMessageWrapper<T> {
  private String operation;
  private List<T> data;
  private UUID correlationId;

  private String dataType;

  public SagaMessageWrapper(String operation, List<T> data, UUID correlationId) {
    this.operation = operation;
    this.data = data;
    this.correlationId = correlationId;
  }

  public static class DataTypes {
    public static final String cliente = "cliente";
    public static final String endereco = "endereco";
    public static final String gerente = "gerente";
    public static final String conta = "conta";
    public static final String itemHistorico = "itemHistorico";
  }

  public static <T> List<T> convertList(List<Object> original, Class<T> tipo) {
    return original.stream()
        .map(tipo::cast)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
  public static <T> SagaMessageWrapper<T> convertWrapper(SagaMessageWrapper<Object> original, Class<T> tipo) {
    return new SagaMessageWrapper<T>(
        original.getOperation(),
        convertList(original.getData(), tipo),
        original.getCorrelationId(),
        original.getDataType());
  }

}
