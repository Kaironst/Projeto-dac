package br.ufpr.dac.orchestratorService.messaging.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.keys.RabbitmqConsts;

@Service
public class UsersProducer {

  @Autowired
  private RabbitTemplate template;

  public MessageWrapper<UsersDto.Cliente> enviarMenssagem(MessageWrapper<UsersDto.Cliente> message) {
    var response = (MessageWrapper<UsersDto.Cliente>) template.convertSendAndReceiveAsType(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.USERS_KEY,
        message,
        new ParameterizedTypeReference<MessageWrapper<UsersDto.Cliente>>() {
        });

    if (response == null)
      System.out.println("error on enviarMenssagem from usersService");
    return response;

  }

};
