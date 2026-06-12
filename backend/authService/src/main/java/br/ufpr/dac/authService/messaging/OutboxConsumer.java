package br.ufpr.dac.authService.messaging;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.ufpr.dac.authService.document.Conta;
import br.ufpr.dac.authService.document.Conta.Roles;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class OutboxConsumer {

  private final ObjectMapper objectMapper;
  private final MongoTemplate mongoTemplate;
  private final PasswordEncoder passwordEncoder;

  @RabbitListener(queues = RabbitmqConsts.AUTH_EVENT_QUEUE)
  private void recieve(Message message) {
    System.out.println("msgreceb");
    try {

      String messageJson = new String(message.getBody(), StandardCharsets.UTF_8);
      // sanitiza a string
      if (messageJson.startsWith("\"") && messageJson.endsWith("\"")) {
        messageJson = messageJson.substring(1, messageJson.length() - 1)
            .replace("\\\"", "\"").replace("\\\\", "\\");
      }
      System.out.println(messageJson);

      JsonNode root = objectMapper.readTree(messageJson);
      JsonNode payload = root.path("payload");
      if (payload.isMissingNode() || payload.isNull()) {
        System.err.println("sem payload");
        return;
      }

      String cpf = payload.path("cpf").asString();
      String email = payload.path("email").asString();
      Long userId = payload.path("userId").asLong();
      String senha = payload.path("senha").asString();
      String operacao = root.path("event_type").asString();

      System.out.println("menssagem válida recebida:" + messageJson);

      List<Roles> roles = new ArrayList<>();
      if (root.path("data_type").asString().equals("cliente")) {
        roles.add(Roles.ROLE_CLIENTE);
      } else if (root.path("data_type").asString().equals("gerente")) {
        roles.add(Roles.ROLE_GERENTE);
        if (payload.path("isAdmin").asBoolean()) {
          roles.add(Roles.ROLE_ADMINISTRADOR);
        }
      }

      if ("deleted".equals(operacao)) {
        handleDelete(userId, roles);
        return;
      }

      handleUpsert(cpf, email, userId, senha, roles);

    } catch (Exception e) {
      System.out.println("error on outbox consumer");
      e.printStackTrace();
    }
  }

  // usando mongoTemplate já que jdbc não funciona para noSQL
  public void handleUpsert(String cpf, String email, Long userId, String senha, List<Roles> roles) {

    Roles roleBase = roles.contains(Roles.ROLE_GERENTE) ? Roles.ROLE_GERENTE : Roles.ROLE_CLIENTE;
    Query query = new Query(Criteria.where("userId").is(userId).and("roles").in(roleBase));

    Update update = new Update()
        .set("email", email)
        .set("cpf", cpf)
        .set("roles", roles);
    if (senha != null && !senha.isEmpty())
      update.set("senha", passwordEncoder.encode(senha));

    FindAndModifyOptions options = new FindAndModifyOptions()
        .upsert(true)
        .returnNew(true);

    mongoTemplate.findAndModify(query, update, options, Conta.class);
    System.out.println("upsert em" + roleBase + " com id " + userId);
  }

  public void handleDelete(Long userId, List<Roles> roles) {

    Roles roleBase = roles.contains(Roles.ROLE_GERENTE) ? Roles.ROLE_GERENTE : Roles.ROLE_CLIENTE;
    Query query = new Query(Criteria.where("userId").is(userId).and("roles").in(roleBase));

    mongoTemplate.remove(query, Conta.class);
    System.out.println("removido" + roleBase + " com id " + userId);
  }

}
