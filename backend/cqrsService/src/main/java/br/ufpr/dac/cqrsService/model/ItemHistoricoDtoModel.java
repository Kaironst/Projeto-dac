package br.ufpr.dac.cqrsService.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.shared.dto.ItemHistoricoDto;
import lombok.AllArgsConstructor;
import tools.jackson.databind.JsonNode;

@Repository
@AllArgsConstructor
public class ItemHistoricoDtoModel implements DebeziumModel {

  private final JdbcClient client;

  public List<ItemHistoricoDto.ItemHistorico> handleRead(Long id) {

    var itemHistorico = client.sql("""
        SELECT * FROM item_historico
        WHERE id=:id
        """)
        .param("id", id)
        .query(ItemHistoricoDto.ItemHistorico.class).list();

    return itemHistorico;
  }

  public List<ItemHistoricoDto.ItemHistorico> handleReadAll() {

    var itensHistorico = client.sql("""
        SELECT * FROM item_historico
        """)
        .query(ItemHistoricoDto.ItemHistorico.class).list();

    return itensHistorico;
  }

  @Override
  public void handleUpsert(JsonNode data) {
    var id = data.path("id").asLong();
    // timestamp(6) without timezone (no debezium microssegundos desde o epoch)
    var data_hora = data.path("data_hora").asLong();
    var tipo = data.path("tipo").asInt();
    var valor_movimentacao = data.path("valor_movimentacao").asDouble();
    var conta_destino_id = data.path("conta_destino_id").asLong();
    var conta_origem_id = data.path("conta_origem_id").asLong();

    client.sql("""
        INSERT INTO item_historico (id, data_hora, tipo, valor_movimentacao, conta_destino_id, conta_origem_id)
        VALUES (:id, :data_hora, :tipo, :valor_movimentacao, :conta_destino_id, :conta_origem_id)
        ON CONFLICT (id)
        DO UPDATE SET
          data_hora = excluded.data_hora,
          tipo = excluded.tipo,
          valor_movimentacao = excluded.valor_movimentacao,
          conta_destino_id = excluded.conta_destino_id,
          conta_origem_id = excluded.conta_origem_id
        """)
        .param("id", id)
        .param("data_hora", LocalDateTime.ofEpochSecond(
            // segundos desdo epoch
            data_hora / 1000000,
            // precisão adicional de nanossegundos
            (int) ((data_hora % 1000000) * 1000),
            ZoneOffset.UTC))
        .param("tipo", tipo)
        .param("valor_movimentacao", valor_movimentacao)
        .param("conta_destino_id", conta_destino_id)
        .param("conta_origem_id", conta_origem_id)
        .update();
  }

  @Override
  public void handleDelete(JsonNode data) {
    var id = data.path("id").asLong();
    client.sql("""
        DELETE FROM item_historico
        WHERE id=:id
        """)
        .param("id", id)
        .update();
  }
}
