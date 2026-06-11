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
    private Boolean administrador;

    // construtor sem senha
    public Gerente(Long id, String nome, String email, String cpf, String telefone, Boolean administrador) {
      this.id = id;
      this.nome = nome;
      this.email = email;
      this.cpf = cpf;
      this.telefone = telefone;
      this.administrador = administrador;

    }

    private String senha;
  }

}
