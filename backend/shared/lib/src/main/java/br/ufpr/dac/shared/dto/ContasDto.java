package br.ufpr.dac.shared.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ContasDto {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Message {
    private String Operation;
    private List<Conta> data;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Conta {
    private Long id;
    private UsersDto.Cliente cliente;
    // adicionar quando gerente estiver pronto
    // private Gerente gerente;
    private Double saldo;
    private Double limite;
    private LocalDate dataCriacao;

  }

}
