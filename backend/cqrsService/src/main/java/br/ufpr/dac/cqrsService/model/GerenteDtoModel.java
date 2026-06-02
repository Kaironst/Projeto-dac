
package br.ufpr.dac.cqrsService.model;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.shared.dto.GerentesDto;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class GerenteDtoModel {

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

}
