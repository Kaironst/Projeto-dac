package br.ufpr.dac.shared.dto;

import java.time.LocalDate;

import br.ufpr.dac.shared.dto.GerentesDto.Gerente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ContasDto {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Conta {
    private Long id;
    private UsersDto.Cliente cliente;
    private Gerente gerente;
    private Double saldo;
    private Double limite;
    private LocalDate dataCriacao;

  }

}
