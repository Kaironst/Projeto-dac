package br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes;

import br.ufpr.dac.orchestratorService.saga.sagaStep;

public enum InsertGerentesPasso implements sagaStep {
  BUSCANDO_GERENTE_COM_MAIS_CONTAS,
  INSERINDO_GERENTE,
  DANDO_CONTA_AO_NOVO_GERENTE,
  CONCLUINDO,
  ROLLING_BACK,
  FINALIZADO
}
