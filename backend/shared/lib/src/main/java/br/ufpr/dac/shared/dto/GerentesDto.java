package br.ufpr.dac.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class GerentesDto {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Gerente {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
  }

}
