package br.ufpr.dac.cqrsService.model;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.shared.dto.ContasDto;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ContaDtoModel {

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

}
