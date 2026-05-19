package br.ufpr.dac.orchestratorService.saga.autocadastroSaga;

import br.ufpr.dac.shared.dto.AutocadastroDto;
import br.ufpr.dac.shared.dto.ContasDto;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.UsersDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutocadastroData {
  private AutocadastroDto.SolicitacaoEntrada entrada;
  private AutocadastroDto.Aprovacao aprovacao;
  private AutocadastroDto.Rejeicao rejeicao;
  private AutocadastroDto.Solicitacao solicitacao;
  private UsersDto.Cliente cliente;
  private GerentesDto.Gerente gerenteResponsavel;
  private ContasDto.Conta contaCriada;
  private AutocadastroDto.UsuarioAuth usuarioAuth;
  private boolean authCriado;
  private AutocadastroDto.Falha falha;
}
