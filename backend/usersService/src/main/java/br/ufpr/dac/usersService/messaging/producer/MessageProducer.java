package br.ufpr.dac.usersService.messaging.producer;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufpr.dac.usersService.config.RabbitMQConfig;
import br.ufpr.dac.usersService.messaging.dto.UsersDto;

@Service
public class MessageProducer {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  /**
   * manda menssagem para orchestratorService com RabbitMQ
   * 
   * @param message menssagem a ser enviada
   */
  public void messageOrchestrator(String operation, List<UsersDto.Cliente> data) {
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.APP_EXCHANGE,
        RabbitMQConfig.ORCHESTRATOR_KEY,
        new UsersDto.Message(operation, data));
  }

}
