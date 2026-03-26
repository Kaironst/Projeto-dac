package br.ufpr.dac.orchestratorService.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.config.RabbitMQConfig;

@Component
public class Consumer {

  @RabbitListener(queues = RabbitMQConfig.ORCHESTRATOR_QUEUE)
  public void recieveMessage(String message) {
    System.out.println("menssagem recebida " + message);
  }

}
