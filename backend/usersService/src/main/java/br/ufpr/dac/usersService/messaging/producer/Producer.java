package br.ufpr.dac.usersService.messaging.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufpr.dac.usersService.config.RabbitMQConfig;

@Service
public class Producer {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  /**
   * manda menssagem para orchestratorService com RabbitMQ
   * 
   * @param message menssagem a ser enviada
   */
  public void messageOrchestrator(String message) {
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.APP_EXCHANGE,
        RabbitMQConfig.ORCHESTRATOR_KEY,
        message);
  }

}
