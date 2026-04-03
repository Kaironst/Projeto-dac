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

    switch (message.getOperation()) {
      case "CREATE" -> {
        return producer.createCliente(message.getData().getFirst());
      }
      case "READ" -> {
        return producer.readCliente(message.getData().getFirst().getId());
      }
      case "READ_ALL" -> {
        return producer.readAllClientes();
      }
      case "UPDATE" -> {
        return producer.updateCliente(message.getData().getFirst());
      }
      case "DELETE" -> {
        return producer.deleteCliente(message.getData().getFirst().getId());
      }
      default -> {
        throw new UnsupportedOperationException();
      }
    }

  }

}
