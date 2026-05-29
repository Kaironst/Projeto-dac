package br.ufpr.dac.orchestratorService.saga.gerentesSaga.removeGerentes;

import br.ufpr.dac.orchestratorService.saga.sagaStep;

public enum RemoveGerentesPasso implements sagaStep {
  BUSCANDO_TODOS_GERENTES,
  BUSCANDO_GERENTE_COM_MENOS_CONTAS,
  DANDO_CONTAS_AO_GERENTE_COM_MENOS,
  REMOVENDO_GERENTE,
  CONCLUINDO,
  FINALIZADO
}
