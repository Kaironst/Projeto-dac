package br.ufpr.dac.gerentesService.entity.listener;

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
      if ("deleted".equals(eventType))
        dataMap = Map.of("userId", gerente.getId());
      else
        dataMap = Map.of(
            "userId", gerente.getId(),
            "email", gerente.getEmail(),
            "senha", gerente.getSenha(),
            "cpf", gerente.getCpf(),
            "isAdmin", gerente.getAdministrador());

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

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
