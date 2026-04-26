package br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes;

import br.ufpr.dac.shared.dto.GerentesDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsertGerentesData {
  private GerentesDto.Gerente GerenteAInserir;
  private Long idGerenteAntigo;
}
