package br.ufpr.dac.cqrsService.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.shared.dto.ContasDto;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.UsersDto;
import lombok.AllArgsConstructor;
import tools.jackson.databind.JsonNode;

@Repository
@AllArgsConstructor
public class ContaDtoModel implements DebeziumModel {

  private final JdbcClient client;

  public ContasDto.Conta handleRead(Long id) {
    return client.sql("""
        SELECT co.id, co.data_criacao, co.limite, co.numero, co.saldo,
               cl.id as cl_id, cl.cpf as cl_cpf, cl.email as cl_email,
               cl.nome as cl_nome, cl.salario as cl_salario, cl.telefone as cl_telefone,
               en.id as en_id, en.logradouro as en_logradouro, en.numero as en_numero,
               en.complemento as en_complemento, en.cidade as en_cidade, en.estado as en_estado, en.cep as en_cep,
               ge.id as ge_id, ge.administrador as ge_administrador, ge.cpf as ge_cpf,
               ge.email as ge_email, ge.nome as ge_nome, ge.telefone as ge_telefone
        FROM conta co
        LEFT JOIN cliente cl on cl.id = co.cliente
        LEFT JOIN endereco en on en.cliente_id = cl.id
        LEFT JOIN gerente ge on ge.id = co.gerente
        WHERE co.id = :id
        """)
        .param("id", id)
        .query(rs -> {
          Map<Long, ContasDto.Conta> contaMap = new LinkedHashMap<>();
          while (rs.next()) {
            long contaId = rs.getLong("id");
            // cria conta
            ContasDto.Conta conta = contaMap.computeIfAbsent(contaId, cid -> {
              try {
                return ContasDto.Conta.builder()
                    .id(cid).numero(rs.getString("numero")).saldo(rs.getDouble("saldo"))
                    .limite(rs.getDouble("limite"))
                    .dataCriacao(rs.getDate("data_criacao") != null ? rs.getDate("data_criacao").toLocalDate() : null)
                    .build();
              } catch (Exception e) {
                throw new RuntimeException("Erro ao mapear conta", e);
              }
            });
            // cria clientes
            long clienteId = rs.getLong("cl_id");
            if (!rs.wasNull() && conta.getCliente() == null) {
              UsersDto.Cliente cliente = UsersDto.Cliente.builder()
                  .id(clienteId).nome(rs.getString("cl_nome")).email(rs.getString("cl_email"))
                  .cpf(rs.getString("cl_cpf")).salario(rs.getDouble("cl_salario")).telefone(rs.getString("cl_telefone"))
                  .enderecos(new ArrayList<UsersDto.Endereco>())
                  .build();
              conta.setCliente(cliente);
            }
            // cria endereços para o cliente removendo os que ja existem
            long enderecoId = rs.getLong("en_id");
            if (!rs.wasNull() && conta.getCliente() != null) {
              UsersDto.Endereco endereco = UsersDto.Endereco.builder()
                  .id(enderecoId).logradouro(rs.getString("en_logradouro")).numero(rs.getInt("en_numero"))
                  .complemento(rs.getString("en_complemento")).cep(rs.getString("en_cep"))
                  .cidade(rs.getString("en_cidade")).estado(rs.getString("en_estado"))
                  .build();
              boolean alreadyExists = conta.getCliente().getEnderecos().stream()
                  .anyMatch(e -> e.getId().equals(enderecoId));
              if (!alreadyExists) {
                conta.getCliente().getEnderecos().add(endereco);
              }
            }
            // cria gerentes
            long gerenteId = rs.getLong("ge_id");
            if (!rs.wasNull() && conta.getGerente() == null) {
              GerentesDto.Gerente gerente = GerentesDto.Gerente.builder()
                  .id(gerenteId).nome(rs.getString("ge_nome")).email(rs.getString("ge_email"))
                  .cpf(rs.getString("ge_cpf")).telefone(rs.getString("ge_telefone"))
                  .administrador(rs.getBoolean("ge_administrador"))
                  .build();
              conta.setGerente(gerente);
            }
          }
          return contaMap.values().stream().findFirst().orElse(null);
        });
  }

  public List<ContasDto.Conta> handleReadAll() {
    return client.sql("""
        SELECT co.id, co.data_criacao, co.limite, co.numero, co.saldo,
               cl.id as cl_id, cl.cpf as cl_cpf, cl.email as cl_email,
               cl.nome as cl_nome, cl.salario as cl_salario, cl.telefone as cl_telefone,
               en.id as en_id, en.logradouro as en_logradouro, en.numero as en_numero,
               en.complemento as en_complemento, en.cidade as en_cidade, en.estado as en_estado, en.cep as en_cep,
               ge.id as ge_id, ge.administrador as ge_administrador, ge.cpf as ge_cpf,
               ge.email as ge_email, ge.nome as ge_nome, ge.telefone as ge_telefone
        FROM conta co
        LEFT JOIN cliente cl on cl.id = co.cliente
        LEFT JOIN endereco en on en.cliente_id = cl.id
        LEFT JOIN gerente ge on ge.id = co.gerente
        """)
        .query(rs -> {
          Map<Long, ContasDto.Conta> contaMap = new LinkedHashMap<>();
          while (rs.next()) {
            long contaId = rs.getLong("id");
            // cria conta
            ContasDto.Conta conta = contaMap.computeIfAbsent(contaId, cid -> {
              try {
                return ContasDto.Conta.builder()
                    .id(cid).numero(rs.getString("numero")).saldo(rs.getDouble("saldo"))
                    .limite(rs.getDouble("limite"))
                    .dataCriacao(rs.getDate("data_criacao") != null ? rs.getDate("data_criacao").toLocalDate() : null)
                    .build();
              } catch (Exception e) {
                throw new RuntimeException("Erro ao mapear conta", e);
              }
            });
            // cria clientes
            long clienteId = rs.getLong("cl_id");
            if (!rs.wasNull() && conta.getCliente() == null) {
              UsersDto.Cliente cliente = UsersDto.Cliente.builder()
                  .id(clienteId).nome(rs.getString("cl_nome")).email(rs.getString("cl_email"))
                  .cpf(rs.getString("cl_cpf")).salario(rs.getDouble("cl_salario")).telefone(rs.getString("cl_telefone"))
                  .enderecos(new ArrayList<UsersDto.Endereco>())
                  .build();
              conta.setCliente(cliente);
            }
            // cria endereços para o cliente removendo os que ja existem
            long enderecoId = rs.getLong("en_id");
            if (!rs.wasNull() && conta.getCliente() != null) {
              UsersDto.Endereco endereco = UsersDto.Endereco.builder()
                  .id(enderecoId).logradouro(rs.getString("en_logradouro")).numero(rs.getInt("en_numero"))
                  .complemento(rs.getString("en_complemento")).cep(rs.getString("en_cep"))
                  .cidade(rs.getString("en_cidade")).estado(rs.getString("en_estado"))
                  .build();
              boolean alreadyExists = conta.getCliente().getEnderecos().stream()
                  .anyMatch(e -> e.getId().equals(enderecoId));
              if (!alreadyExists) {
                conta.getCliente().getEnderecos().add(endereco);
              }
            }
            // cria gerentes
            long gerenteId = rs.getLong("ge_id");
            if (!rs.wasNull() && conta.getGerente() == null) {
              GerentesDto.Gerente gerente = GerentesDto.Gerente.builder()
                  .id(gerenteId).nome(rs.getString("ge_nome")).email(rs.getString("ge_email"))
                  .cpf(rs.getString("ge_cpf")).telefone(rs.getString("ge_telefone"))
                  .administrador(rs.getBoolean("ge_administrador"))
                  .build();
              conta.setGerente(gerente);
            }
          }
          return new ArrayList<>(contaMap.values());
        });

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
