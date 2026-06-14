package br.ufpr.dac.orchestratorService.saga.clientesSaga.atualizarLimite;

import br.ufpr.dac.orchestratorService.saga.sagaStep;

public enum AtualizarLimitePasso implements sagaStep {
  ATUALIZANDO_CLIENTE,
  ATUALIZANDO_LIMITE,
  CONCLUINDO,
  FINZALIZADO
}
