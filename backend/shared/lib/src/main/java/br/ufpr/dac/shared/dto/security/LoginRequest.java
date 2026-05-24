package br.ufpr.dac.shared.dto.security;

import lombok.Data;

@Data
public class LoginRequest {
  private String username;
  private String password;
}
