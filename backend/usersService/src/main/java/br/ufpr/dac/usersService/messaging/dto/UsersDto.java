package br.ufpr.dac.usersService.messaging.dto;

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
    private long id;
    private String nome;
    private String email;
    private String cpf;
    private int estado;
    private String telefone;
    private int salario;
    private List<Endereco> enderecos;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Endereco {
    private long id;

    private String logradouro;
    private int numero;
    private String complemento;
    private String cep;
    private String cidade;
    private String estado;
  }

}
