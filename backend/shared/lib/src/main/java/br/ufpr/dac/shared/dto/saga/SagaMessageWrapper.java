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
        original.getCorrelationId());
  }

}
