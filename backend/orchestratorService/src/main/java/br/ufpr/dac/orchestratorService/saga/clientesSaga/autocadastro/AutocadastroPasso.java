package br.ufpr.dac.orchestratorService.saga.clientesSaga.autocadastro;

import br.ufpr.dac.orchestratorService.saga.sagaStep;

public enum AutocadastroPasso implements sagaStep {
  SALVANDO_CLIENTE,
  BUSCANDO_GERENTE_COM_MENOS_CONTAS,
  CRIANDO_CONTA,
  CONCLUINDO,
  FINALIZADO
}
