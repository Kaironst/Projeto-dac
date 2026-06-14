package br.ufpr.dac.orchestratorService.saga.clientesSaga.autocadastro;

import br.ufpr.dac.shared.dto.UsersDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutocadastroData {
  private UsersDto.Cliente clienteAInserir;
  private Long idClienteInserido;
  private Long gerenteComMenosClientesId;
}
