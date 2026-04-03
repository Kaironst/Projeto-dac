package br.ufpr.dac.apiGatewayService.messaging.producer;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import br.ufpr.dac.apiGatewayService.config.RabbitMQConfig;
import br.ufpr.dac.apiGatewayService.messaging.dto.UsersDto;

@Service
public class MessageProducer {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  /**
   * manda menssagem para orchestratorService com RabbitMQ
   * 
   * @param message menssagem a ser enviada
   */
  public List<UsersDto.Cliente> RequestOrchestrator(String operation, List<UsersDto.Cliente> data) {
    var result = (UsersDto.Message) rabbitTemplate.convertSendAndReceiveAsType(
        RabbitMQConfig.APP_EXCHANGE,
        RabbitMQConfig.ORCHESTRATOR_KEY,
        new UsersDto.Message(operation, data),
        new ParameterizedTypeReference<UsersDto.Message>() {
        });

    System.out.println(result);

    return result.getData();

  }

}
