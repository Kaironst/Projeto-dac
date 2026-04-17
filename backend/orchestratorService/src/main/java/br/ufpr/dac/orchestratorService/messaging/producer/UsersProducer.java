package br.ufpr.dac.orchestratorService.messaging.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;

@Service
public class UsersProducer {

  @Autowired
  private RabbitTemplate template;

  public UsersDto.Message enviarMenssagem(UsersDto.Message message) {
    var response = (UsersDto.Message) template.convertSendAndReceiveAsType(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.USERS_KEY,
        message,
        new ParameterizedTypeReference<UsersDto.Message>() {
        });

    if (response == null)
      System.out.println("error on enviarMenssagem from usersService");
    return response == null ? new UsersDto.Message(MessageOperations.ERROR_GENERIC, null) : response;

  }

};
