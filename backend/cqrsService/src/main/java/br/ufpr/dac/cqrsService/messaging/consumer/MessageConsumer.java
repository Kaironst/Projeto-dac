package br.ufpr.dac.cqrsService.messaging.consumer;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.cqrsService.model.ClienteDtoModel;
import br.ufpr.dac.cqrsService.model.ContaDtoModel;
import br.ufpr.dac.cqrsService.model.DebeziumModel;
import br.ufpr.dac.cqrsService.model.EnderecoDtoModel;
import br.ufpr.dac.cqrsService.model.GerenteDtoModel;
import br.ufpr.dac.cqrsService.model.ItemHistoricoDtoModel;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class MessageConsumer {

  private final ObjectMapper objectMapper;

  private final ClienteDtoModel clienteDtoModel;
  private final EnderecoDtoModel enderecoDtoModel;
  private final GerenteDtoModel gerenteDtoModel;
  private final ContaDtoModel contaDtoModel;
  private final ItemHistoricoDtoModel itemHistoricoDtoModel;

  private Map<String, DebeziumModel> routes = null;

  @PostConstruct
  private void buildRoutes() {
    this.routes = Map.of(
        "contas_schema.conta", contaDtoModel,
        "contas_schema.item_historico", itemHistoricoDtoModel,
        "users_schema.endereco", enderecoDtoModel,
        "users_schema.cliente", clienteDtoModel,
        "gerentes_schema.gerente", gerenteDtoModel);
  }

  @RabbitListener(queues = RabbitmqConsts.CQRS_QUEUE)
  public void recieve(String messageJson) {

    try {

      // trata a string como json e busca pelo nó payload
      JsonNode payload = objectMapper.readTree(messageJson).path("payload");
      // extrai informações sobre para onde enviar a atualização
      String table = payload.path("source").path("table").asString();
      String schema = payload.path("source").path("schema").asString();
      String target = schema + "." + table;
      String opcode = payload.path("op").asString();

      if (payload.isMissingNode() || payload.isNull()) {
        System.err.println("menssagem sem paylaod");
        return;
      }

      // nó com os dados novos do banco dentro do payload do Debezium
      JsonNode depois = payload.path("after");
      // nó com os dados anteriores do banco dentro do payload do Debezium
      JsonNode antes = payload.path("before");

      if (("c".equals(opcode) || "u".equals(opcode) || "r".equals(opcode)) && !depois.isNull()) {
        routes.get(target).handleUpsert(depois);
      } else if ("d".equals(opcode)) {
        routes.get(target).handleDelete(antes);
      }

    } catch (Exception e) {
      System.out.println("error on message consumer listener");
      e.printStackTrace();
    }
  }

}
