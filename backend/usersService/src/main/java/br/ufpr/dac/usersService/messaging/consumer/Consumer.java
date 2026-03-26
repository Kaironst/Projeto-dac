package br.ufpr.dac.usersService.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.usersService.config.RabbitMQConfig;

@Component
public class Consumer {

  @RabbitListener(queues = RabbitMQConfig.USERS_QUEUE)
  public void recieve(String message) {
    System.out.println("users service recebeu " + message);
  }
}
