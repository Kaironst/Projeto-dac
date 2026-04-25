package br.ufpr.dac.shared.dto.saga;

import java.util.List;
import java.util.UUID;

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
}
