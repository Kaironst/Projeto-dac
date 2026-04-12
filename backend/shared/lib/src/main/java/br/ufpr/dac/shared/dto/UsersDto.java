package br.ufpr.dac.shared.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UsersDto {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Message {
    private String operation;
    private List<Cliente> data;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Cliente {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private Integer estado;
    private String telefone;
    private Double salario;
    private List<Endereco> enderecos;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Endereco {
    private Long id;

    private String logradouro;
    private Integer numero;
    private String complemento;
    private String cep;
    private String cidade;
    private String estado;
  }

}
