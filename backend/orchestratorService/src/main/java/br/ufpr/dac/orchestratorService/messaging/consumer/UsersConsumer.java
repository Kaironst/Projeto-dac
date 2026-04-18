package br.ufpr.dac.orchestratorService.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.messaging.producer.UsersProducer;
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;

@Component
public class UsersConsumer {

  @Autowired
  UsersProducer producer;

  @RabbitListener(queues = RabbitmqConsts.ORCHESTRATOR_USERS_QUEUE)
  public UsersDto.Message recieveMessage(UsersDto.Message message) {
    try {
      System.out.println(message);
      UsersDto.Message response = producer.enviarMenssagem(message);

      if (response == null)
        System.out.println("error on usersConsumer");
      return response == null ? new UsersDto.Message(MessageOperations.ERROR_GENERIC, null) : response;

    } catch (Exception e) {
      System.out.println("error on usersConsumer");
      e.printStackTrace();
      return new UsersDto.Message(MessageOperations.ERROR_GENERIC, null);
    }

  }

}
