package br.ufpr.dac.shared.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class GerentesDto {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Message {
    private String operation;
    private List<Gerente> data;
  }

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
