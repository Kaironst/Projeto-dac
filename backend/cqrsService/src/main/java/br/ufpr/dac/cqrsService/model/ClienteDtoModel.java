package br.ufpr.dac.cqrsService.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

  // usando extrator para evitar n+1 e overhead de memória
  public List<UsersDto.Cliente> handleReadAll() {

    return client.sql("""
        SELECT c.id as id, c.nome as nome, c.email as email, c.cpf as cpf, c.estado as estado, c.telefone as telefone,
        c.salario as salario, e.id as e_id, e.logradouro as logradouro, e.numero as numero,
        e.complemento as complemento, e.cep as cep, e.cidade as cidade, e.estado as uf
        FROM cliente c
        LEFT JOIN endereco e ON c.id = e.id
        """)
        .query(rs -> {
          Map<Long, UsersDto.Cliente> clienteMap = new LinkedHashMap<>();

          while (rs.next()) {
            var clienteId = rs.getLong("id");
            // cria cliente não adicionado
            UsersDto.Cliente cliente = clienteMap.computeIfAbsent(clienteId, id -> {
              try {
                return UsersDto.Cliente.builder().id(id).nome(rs.getString("nome")).email(rs.getString("email"))
                    .cpf(rs.getString("cpf")).estado(rs.getInt("estado")).telefone(rs.getString("telefone"))
                    .salario(rs.getDouble("salario")).enderecos(new ArrayList<>()).build();
              } catch (Exception e) {
                throw new RuntimeException("erro mapeando cliente", e);
              }
            });
            // adiciona endereco
            long enderecoId = rs.getLong("e_id");
            if (!rs.wasNull()) {
              UsersDto.Endereco endereco = UsersDto.Endereco.builder()
                  .id(enderecoId).logradouro(rs.getString("logradouro")).numero(rs.getInt("numero"))
                  .complemento(rs.getString("complemento")).cep(rs.getString("cep"))
                  .cidade(rs.getString("cidade")).estado(rs.getString("uf")).build();
              cliente.getEnderecos().add(endereco);
            }
          }
          return new ArrayList<>(clienteMap.values());

        });
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
