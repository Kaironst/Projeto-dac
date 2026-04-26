package br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes;

import br.ufpr.dac.orchestratorService.saga.sagaStep;

public enum InsertGerentesPasso implements sagaStep {
  INICIO,
  GERENTE_COM_MAIS_CLIENTES_BUSCADO,
  GERENTE_INSERIDO,
  CONTA_DADA_AO_NOVO_GERENTE,
  CONCLUIDO,
  ROLLED_BACK
}
