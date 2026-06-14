package br.ufpr.dac.orchestratorService.saga.clientesSaga.atualizarLimite;

import br.ufpr.dac.shared.dto.UsersDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtualizarLimiteData {
  private UsersDto.Cliente clienteAntigo;
  private UsersDto.Cliente clienteAtualizado;
}
