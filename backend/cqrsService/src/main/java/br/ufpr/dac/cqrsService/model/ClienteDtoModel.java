package br.ufpr.dac.cqrsService.model;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.shared.dto.UsersDto;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class ClienteDtoModel {

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

}
