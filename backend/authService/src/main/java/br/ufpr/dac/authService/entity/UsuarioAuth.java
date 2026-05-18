package br.ufpr.dac.authService.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "usuarios")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioAuth {

  @Id
  private String id;

  private Long clienteId;

  @Indexed(unique = true)
  private String email;

  private String senhaHash;
  private String tipo;

}
