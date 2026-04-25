package br.ufpr.dac.orchestratorService.saga;

public enum SagaStatus {
  RUNNING,
  SUCCESS,
  FAILED,
  COMPENSATING
}
