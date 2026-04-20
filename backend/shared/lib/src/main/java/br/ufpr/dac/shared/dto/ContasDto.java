package br.ufpr.dac.shared.dto;

import java.time.LocalDate;
import java.util.List;

import br.ufpr.dac.shared.dto.GerentesDto.Gerente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

public class ContasDto {

  @Data
  @EqualsAndHashCode(callSuper = false)
  @SuperBuilder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Message extends MessageWrapper {
    private List<Conta> data;

    public Message(String operation, List<Conta> data) {
      super(operation);
      this.data = data;
    }

  }

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
