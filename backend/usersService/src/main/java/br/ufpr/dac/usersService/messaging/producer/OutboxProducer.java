package br.ufpr.dac.usersService.messaging.producer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class OutboxProducer {

  private final JdbcClient jdbcClient;
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  public void writeToOutbox(String eventType, UsersDto.Cliente cliente) {
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

  @Scheduled(fixedDelay = 500)
  @Transactional
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
      String dataJson = row.get("data") != null ? row.get("data").toString() : null;

      try {

        Object dataTree = objectMapper.readTree(dataJson);

        Map<String, Object> message = new HashMap<>();
        message.put("event_type", eventType);
        message.put("data_type", dataType);
        message.put("data_id", dataId);
        message.put("payload", dataTree);
        String messageString = objectMapper.writeValueAsString(message);

        String routingKey = "events.cliente." + eventType;
        rabbitTemplate.convertAndSend(RabbitmqConsts.AUTH_EVENT_EXCHANGE, routingKey, messageString);

        jdbcClient.sql("DELETE FROM outbox WHERE id = :id")
            .param("id", id)
            .update();

        System.out.println("MENSSAGEM OUTBOX ENVIADA (\" " + messageString + " \")");

      } catch (Exception e) {
        e.printStackTrace();
      }

    }

  }

}
