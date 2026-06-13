package br.ufpr.dac.orchestratorService.saga.gerentesSaga.removeGerentes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoveGerentesData {
  Long idGerenteARemover;
  Long idGerenteComMenosContas;
  List<Long> contasMovidas;
}
