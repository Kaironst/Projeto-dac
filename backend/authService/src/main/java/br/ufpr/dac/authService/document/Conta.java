package br.ufpr.dac.authService.document;

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

  private String email;

  @Indexed(unique = true)
  private String accountId;

  private String senha;
  private String salt;

}
