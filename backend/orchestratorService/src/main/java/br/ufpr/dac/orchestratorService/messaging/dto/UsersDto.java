package br.ufpr.dac.orchestratorService.messaging.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

public class UsersDto {

  @Data
  @Builder
  public static class Message {
    private String operation;
    private List<Cliente> data;
  }

  @Data
  @Builder
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
