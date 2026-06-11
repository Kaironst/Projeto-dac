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
  public static class Cliente {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private Integer estado;
    private String telefone;
    private Double salario;
    private List<Endereco> enderecos;

    // construtor sem senha
    public Cliente(Long id, String nome, String email, String cpf, Integer estado, String telefone, Double salario,
        List<Endereco> enderecos) {
      this.id = id;
      this.nome = nome;
      this.email = email;
      this.cpf = cpf;
      this.estado = estado;
      this.telefone = telefone;
      this.salario = salario;
      this.enderecos = enderecos;
    }

    private String senha;

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
