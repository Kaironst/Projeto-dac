package br.ufpr.dac.orchestratorService.messaging.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufpr.dac.orchestratorService.config.RabbitMQConfig;

@Service
public class Producer {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  /**
   * manda menssagem para UsersService com RabbitMQ
   * 
   * @param message menssagem a ser enviada
   */
  public void messageUsers(String message) {
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.APP_EXCHANGE,
        RabbitMQConfig.USERS_KEY,
        message);
  }

}
