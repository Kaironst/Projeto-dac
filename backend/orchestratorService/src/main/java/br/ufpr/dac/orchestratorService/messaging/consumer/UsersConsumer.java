package br.ufpr.dac.orchestratorService.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.messaging.producer.UsersProducer;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;

@Component
public class UsersConsumer {

  @Autowired
  UsersProducer producer;

  @RabbitListener(queues = RabbitmqConsts.ORCHESTRATOR_USERS_QUEUE)
  public MessageWrapper<UsersDto.Cliente> recieveMessage(MessageWrapper<UsersDto.Cliente> message) {
    try {
      System.out.println(message);
      MessageWrapper<UsersDto.Cliente> response = producer.enviarMenssagem(message);

      if (response == null)
        System.out.println("error on usersConsumer");
      return response;

    } catch (Exception e) {
      System.out.println("error on usersConsumer");
      e.printStackTrace();
      return new MessageWrapper<UsersDto.Cliente>(MessageOperations.ERROR_GENERIC, null);
    }

  }

}
