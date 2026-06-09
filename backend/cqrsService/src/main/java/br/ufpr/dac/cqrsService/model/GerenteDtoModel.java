
package br.ufpr.dac.cqrsService.model;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.shared.dto.GerentesDto;
import lombok.AllArgsConstructor;
import tools.jackson.databind.JsonNode;

@Repository
@AllArgsConstructor
public class GerenteDtoModel implements DebeziumModel {

  private final JdbcClient client;

  public GerentesDto.Gerente handleRead(Long id) {

    var gerente = client.sql("""
        SELECT * FROM gerente
        WHERE id=:id
        """)
        .param("id", id)
        .query(GerentesDto.Gerente.class).single();

    return gerente;
  }

  public GerentesDto.Gerente handleReadByCpf(String cpf) {

    var gerente = client.sql("""
        SELECT * FROM gerente
        WHERE cpf=:cpf
        """)
        .param("cpf", cpf)
        .query(GerentesDto.Gerente.class).single();

    return gerente;
  }

  public GerentesDto.Gerente handleReadByEmail(String email) {

    var gerente = client.sql("""
        SELECT * FROM gerente
        WHERE email=:email
        """)
        .param("email", email)
        .query(GerentesDto.Gerente.class).single();

    return gerente;
  }

  public List<GerentesDto.Gerente> handleReadAll() {

    var gerentes = client.sql("""
        SELECT * FROM gerente
        """)
        .query(GerentesDto.Gerente.class).list();

    return gerentes;
  }

  @Override
  public void handleUpsert(JsonNode data) {
    var id = data.path("id").asLong();
    var cpf = data.path("cpf").asString();
    var email = data.path("email").asString();
    var nome = data.path("nome").asString();
    var telefone = data.path("telefone").asString();
    var administrador = data.path("administrador").asBoolean();

    client.sql("""
        INSERT INTO gerente (id, cpf, email, nome, telefone, administrador)
        VALUES (:id, :cpf, :email, :nome, :telefone, :administrador)
        ON CONFLICT (id)
        DO UPDATE SET
          cpf = excluded.cpf,
          email = excluded.email,
          nome = excluded.nome,
          telefone = excluded.telefone,
          administrador = excluded.administrador
        """)
        .param("id", id)
        .param("cpf", cpf)
        .param("email", email)
        .param("nome", nome)
        .param("telefone", telefone)
        .param("administrador", administrador)
        .update();
  }

  @Override
  public void handleDelete(JsonNode data) {
    var id = data.path("id").asLong();
    client.sql("""
        DELETE FROM gerente
        WHERE id=:id
        """)
        .param("id", id)
        .update();
  }

}
