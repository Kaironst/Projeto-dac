package br.ufpr.dac.cqrsService.model;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.shared.dto.UsersDto;
import lombok.AllArgsConstructor;
import tools.jackson.databind.JsonNode;

@Repository
@AllArgsConstructor
public class ClienteDtoModel implements DebeziumModel {

  private final JdbcClient client;

  public UsersDto.Cliente handleRead(Long id) {

    var cliente = client.sql("""
        SELECT * FROM cliente
        WHERE id=:id
        """)
        .param("id", id)
        .query(UsersDto.Cliente.class).single();

    var enderecos = client.sql("""
        SELECT * FROM endereco
        WHERE cliente_id=:id
        """)
        .param("id", id)
        .query(UsersDto.Endereco.class)
        .list();

    cliente.setEnderecos(enderecos);

    return cliente;
  }

  @Override
  public void handleUpsert(JsonNode data) {
    var id = data.path("id").asLong();
    var cpf = data.path("cpf").asString();
    var email = data.path("email").asString();
    var estado = data.path("estado").asInt();
    var nome = data.path("nome").asString();
    var salario = data.path("salario").asDouble();
    var telefone = data.path("telefone").asString();

    client.sql("""
        INSERT INTO cliente (id, cpf, email, estado, nome, salario, telefone)
        VALUES (:id, :cpf, :email, :estado, :nome, :salario, :telefone)
        ON CONFLICT (id)
        DO UPDATE SET
          cpf = excluded.cpf,
          email = excluded.email,
          estado = excluded.estado,
          nome = excluded.nome,
          salario = excluded.salario,
          telefone = excluded.telefone
        """)
        .param("id", id)
        .param("cpf", cpf)
        .param("email", email)
        .param("estado", estado)
        .param("nome", nome)
        .param("salario", salario)
        .param("telefone", telefone)
        .update();
  }

  @Override
  public void handleDelete(JsonNode data) {
    var id = data.path("id").asLong();
    client.sql("""
        DELETE FROM cliente
        WHERE id=:id
        """)
        .param("id", id)
        .update();
  }

}
