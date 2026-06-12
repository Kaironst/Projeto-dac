package br.ufpr.dac.gerentesService.entity.listener;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import br.ufpr.dac.gerentesService.entity.Gerente;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import tools.jackson.databind.ObjectMapper;

@Component
public class GerenteOutboxListener {

  public GerenteOutboxListener(@Lazy JdbcClient jdbcClient, ObjectMapper objectMapper) {
    this.jdbcClient = jdbcClient;
    this.objectMapper = objectMapper;
  }

  private final JdbcClient jdbcClient;
  private final ObjectMapper objectMapper;

  @PostPersist
  public void onGerenteCreated(Gerente gerente) {
    writeToOutbox("created", gerente);
  }

  @PostUpdate
  public void onGerenteUpdated(Gerente gerente) {
    writeToOutbox("updated", gerente);
  }

  @PostRemove
  public void onGerenteDeleted(Gerente gerente) {
    writeToOutbox("deleted", gerente);
  }

  private void writeToOutbox(String eventType, Gerente gerente) {
    try {

      Map<String, Object> dataMap = null;
      if ("deleted".equals(eventType)) {
        dataMap = new HashMap<String, Object>();
        dataMap.put("userId", gerente.getId());
      } else {
        dataMap = new HashMap<String, Object>();
        dataMap.put("userId", gerente.getId());
        dataMap.put("email", gerente.getEmail());
        dataMap.put("senha", gerente.getSenha());
        dataMap.put("cpf", gerente.getCpf());
        dataMap.put("isAdmin", gerente.getAdministrador());
      }

      String dataJson = objectMapper.writeValueAsString(dataMap);

      jdbcClient.sql("""
          INSERT INTO outbox (event_type, data_type, data_id, data)
          VALUES(:event_type, :data_type, :data_id, CAST(:data AS jsonb))
          """)
          .param("event_type", eventType)
          .param("data_type", "gerente")
          .param("data_id", gerente.getId())
          .param("data", dataJson)
          .update();

      System.out.println("transação inserida no outbox");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
