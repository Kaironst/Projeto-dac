package br.ufpr.dac.authService.document;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Conta {

  @Id
  private String id;

  private String userId;
  @Indexed(unique = true)
  private String email;
  @Indexed(unique = true)
  private String cpf;

  private List<String> roles;

  private String senha;

}
