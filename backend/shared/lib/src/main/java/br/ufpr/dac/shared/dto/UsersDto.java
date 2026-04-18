package br.ufpr.dac.shared.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

public class UsersDto {

  @Data
  @EqualsAndHashCode(callSuper = true)
  @SuperBuilder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Message extends MessageWrapper {
    private List<Cliente> data;

    public Message(String operation, List<Cliente> data) {
      super(operation);
      this.data = data;
    }

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
