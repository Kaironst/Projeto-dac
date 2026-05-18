package br.ufpr.dac.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDto {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class LoginRequest {
    private String email;
    private String senha;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class LoginResponse {
    private String token;
    private String tokenType;
    private String tipo;
    private String email;
    private Long clienteId;
  }

}
