package br.ufpr.dac.orchestratorService.saga.gerentesSaga.removeGerentes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoveGerentesData {
  Long idGerenteARemover;
  Long idGerenteComMenosContas;
}
