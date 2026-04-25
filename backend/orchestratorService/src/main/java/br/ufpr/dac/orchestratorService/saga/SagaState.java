package br.ufpr.dac.orchestratorService.saga;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SagaState {
  private UUID correlationId;
  private sagaStep step;
  private SagaStatus status;
}
