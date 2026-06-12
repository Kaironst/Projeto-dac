package br.ufpr.dac.usersService.entity.listener;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import br.ufpr.dac.usersService.entity.Cliente;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import tools.jackson.databind.ObjectMapper;

@Component
public class ClienteOutboxListener {

  public ClienteOutboxListener(@Lazy JdbcClient jdbcClient, ObjectMapper objectMapper) {
    this.jdbcClient = jdbcClient;
    this.objectMapper = objectMapper;
  }

  private final JdbcClient jdbcClient;
  private final ObjectMapper objectMapper;

  @PostPersist
  public void onClienteCreated(Cliente cliente) {
    writeToOutbox("created", cliente);
  }

  @PostUpdate
  public void onClienteUpdated(Cliente cliente) {
    writeToOutbox("updated", cliente);
  }

  @PostRemove
  public void onClienteDeleted(Cliente cliente) {
    writeToOutbox("deleted", cliente);
  }

  private void writeToOutbox(String eventType, Cliente cliente) {
    try {

      Map<String, Object> dataMap = null;
      if ("deleted".equals(eventType)) {
        dataMap = new HashMap<String, Object>();
        dataMap.put("userId", cliente.getId());
      } else {
        dataMap = new HashMap<String, Object>();
        dataMap.put("userId", cliente.getId());
        dataMap.put("email", cliente.getEmail());
        dataMap.put("senha", cliente.getSenha());
        dataMap.put("cpf", cliente.getCpf());
      }

      String dataJson = objectMapper.writeValueAsString(dataMap);

      jdbcClient.sql("""
          INSERT INTO outbox (event_type, data_type, data_id, data)
          VALUES(:event_type, :data_type, :data_id, CAST(:data AS jsonb))
          """)
          .param("event_type", eventType)
          .param("data_type", "cliente")
          .param("data_id", cliente.getId())
          .param("data", dataJson)
          .update();

      System.out.println("transação inserida no outbox");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
