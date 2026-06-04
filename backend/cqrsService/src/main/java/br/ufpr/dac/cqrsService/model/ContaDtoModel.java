package br.ufpr.dac.cqrsService.model;

import java.time.LocalDate;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.shared.dto.ContasDto;
import lombok.AllArgsConstructor;
import tools.jackson.databind.JsonNode;

@Repository
@AllArgsConstructor
public class ContaDtoModel implements DebeziumModel {

  private final JdbcClient client;

  public ContasDto.Conta handleRead(Long id) {

    var conta = client.sql("""
        SELECT * FROM conta
        WHERE id=:id
        """)
        .param("id", id)
        .query(ContasDto.Conta.class).single();

    return conta;
  }

  @Override
  public void handleUpsert(JsonNode data) {
    var id = data.path("id").asLong();
    var cliente = data.path("cliente").asLong();
    // data no debezium vem como dias desde o epoch
    var data_criacao = data.path("data_criacao").asLong();
    var gerente = data.path("gerente").asLong();
    var limite = data.path("limite").asDouble();
    var numero = data.path("numero").asString();
    var saldo = data.path("saldo").asDouble();

    client.sql("""
        INSERT INTO conta (id, cliente, data_criacao, gerente, limite, numero, saldo)
        VALUES (:id, :cliente, :data_criacao, :gerente, :limite, :numero, :saldo)
        ON CONFLICT (id)
        DO UPDATE SET
          cliente = excluded.cliente,
          data_criacao = excluded.data_criacao,
          gerente = excluded.gerente,
          limite = excluded.limite,
          numero = excluded.numero,
          saldo = excluded.saldo
        """)
        .param("id", id)
        .param("cliente", cliente)
        .param("data_criacao", LocalDate.ofEpochDay(data_criacao))
        .param("gerente", gerente)
        .param("limite", limite)
        .param("numero", numero)
        .param("saldo", saldo)
        .update();
  }

  @Override
  public void handleDelete(JsonNode data) {
    var id = data.path("id").asLong();
    client.sql("""
        DELETE FROM conta
        WHERE id=:id
        """)
        .param("id", id)
        .update();
  }

}
