package br.ufpr.dac.cqrsService.model;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.shared.dto.ItemHistoricoDto;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ItemHistoricoDtoModel {

  private final JdbcClient client;

  public ItemHistoricoDto.ItemHistorico handleRead(Long id) {

    var itemHistorico = client.sql("""
        SELECT * FROM item_historico
        WHERE id=:id
        """)
        .param("id", id)
        .query(ItemHistoricoDto.ItemHistorico.class).single();

    return itemHistorico;
  }

}
