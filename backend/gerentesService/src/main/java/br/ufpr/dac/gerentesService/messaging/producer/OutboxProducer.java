package br.ufpr.dac.gerentesService.messaging.producer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class OutboxProducer {

  private final JdbcClient jdbcClient;
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelay = 500)
  public void processOutbox() {

    List<Map<String, Object>> rows = jdbcClient.sql("""
        SELECT id, event_type, data_type, data_id, data FROM outbox
        ORDER BY created_at ASC
        LIMIT 50
        FOR UPDATE SKIP LOCKED
        """)
        .query()
        .listOfRows();

    if (rows.isEmpty())
      return;

    for (Map<String, Object> row : rows) {
      UUID id = (UUID) row.get("id");
      String eventType = (String) row.get("event_type");
      String dataType = (String) row.get("data_type");
      Long dataId = (Long) row.get("data_id");
      String dataJson = (String) row.get("data");

      try {

        Object dataTree = objectMapper.readTree(dataJson);

        Map<String, Object> message = Map.of(
            "event_type", eventType,
            "data_type", dataType,
            "data_id", dataId,
            "payload", dataTree);
        String messageString = objectMapper.writeValueAsString(message);

        String routingKey = "events.gerente" + eventType;
        rabbitTemplate.convertAndSend(RabbitmqConsts.AUTH_EVENT_EXCHANGE, routingKey, messageString);

        jdbcClient.sql("DELETE FROM outbox WHERE id = :id")
            .param("id", id)
            .update();

      } catch (Exception e) {
        e.printStackTrace();
      }

    }

  }

}
