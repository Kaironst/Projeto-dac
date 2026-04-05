package br.ufpr.dac.orchestratorService.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.config.RabbitMQConfig;
import br.ufpr.dac.orchestratorService.messaging.dto.UsersDto;
import br.ufpr.dac.orchestratorService.messaging.producer.UsersProducer;

@Component
public class UsersConsumer {

  @Autowired
  UsersProducer producer;

  @RabbitListener(queues = RabbitMQConfig.ORCHESTRATOR_QUEUE)
  public UsersDto.Message recieveMessage(UsersDto.Message message) {
    try {
      UsersDto.Message response = switch (message.getOperation()) {
        case "CREATE" -> producer.createCliente(message.getData().getFirst());
        case "READ" -> producer.readCliente(message.getData().getFirst().getId());
        case "READ_ALL" -> producer.readAllClientes();
        case "UPDATE" -> producer.updateCliente(message.getData().getFirst());
        case "DELETE" -> producer.deleteCliente(message.getData().getFirst().getId());
        default -> throw new UnsupportedOperationException();
      };

      if (response == null)
        System.out.println("error on usersConsumer");
      return response == null ? new UsersDto.Message("ERROR", null) : response;

    } catch (Exception e) {
      System.out.println("error on usersConsumer");
      e.printStackTrace();
      return new UsersDto.Message("ERROR", null);
    }

  }

}
