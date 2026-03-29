package br.ufpr.dac.orchestratorService.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.config.RabbitMQConfig;
import br.ufpr.dac.orchestratorService.messaging.dto.UsersDto;

@Component
public class UsersConsumer {

  @RabbitListener(queues = RabbitMQConfig.ORCHESTRATOR_QUEUE)
  public void recieveMessage(UsersDto.Message message) {
    System.out.println("menssagem recebida " + message);
    // TODO: implementar passar resultado para api gateway aqui
  }

}
