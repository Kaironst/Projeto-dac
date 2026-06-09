package br.ufpr.dac.authService.document;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Conta {

  public static enum Roles {
    ROLE_CLIENTE,
    ROLE_GERENTE,
    ROLE_ADMINISTRADOR,
  }

  @Id
  private String id;

  private Long userId;
  @Indexed(unique = true)
  private String email;
  @Indexed(unique = true)
  private String cpf;

  private List<Roles> roles;

  private String senha;

}
