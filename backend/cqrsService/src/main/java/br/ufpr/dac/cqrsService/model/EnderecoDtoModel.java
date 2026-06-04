package br.ufpr.dac.cqrsService.model;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.shared.dto.UsersDto;
import lombok.AllArgsConstructor;
import tools.jackson.databind.JsonNode;

@Repository
@AllArgsConstructor
public class EnderecoDtoModel implements DebeziumModel {

  private final JdbcClient client;

  public UsersDto.Endereco handleRead(Long id) {

    var endereco = client.sql("""
        SELECT * FROM endereco
        WHERE id=:id
        """)
        .param("id", id)
        .query(UsersDto.Endereco.class).single();

    return endereco;
  }

  @Override
  public void handleUpsert(JsonNode data) {
    var id = data.path("id").asLong();
    var cep = data.path("cep").asString();
    var cidade = data.path("cidade").asString();
    var complemento = data.path("complemento").asString();
    var estado = data.path("estado").asString();
    var logradouro = data.path("logradouro").asString();
    var numero = data.path("numero").asInt();
    var cliente_id = data.path("cliente_id").asLong();

    client.sql("""
        INSERT INTO endereco (id, cep, cidade, complemento, estado, logradouro, numero, cliente_id)
        VALUES (:id, :cep, :cidade, :complemento, :estado, :logradouro, :numero, :cliente_id)
        ON CONFLICT (id)
        DO UPDATE SET
          cep = excluded.cep,
          cidade = excluded.cidade,
          complemento = excluded.complemento,
          estado = excluded.estado,
          logradouro = excluded.logradouro,
          numero = excluded.numero,
          cliente_id = excluded.cliente_id
        """)
        .param("id", id)
        .param("cep", cep)
        .param("cidade", cidade)
        .param("complemento", complemento)
        .param("estado", estado)
        .param("logradouro", logradouro)
        .param("numero", numero)
        .param("cliente_id", cliente_id)
        .update();
  }

  @Override
  public void handleDelete(JsonNode data) {
    var id = data.path("id").asLong();
    client.sql("""
        DELETE FROM endereco
        WHERE id=:id
        """)
        .param("id", id)
        .update();
  }

}
