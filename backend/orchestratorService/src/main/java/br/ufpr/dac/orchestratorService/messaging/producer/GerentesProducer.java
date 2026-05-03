package br.ufpr.dac.orchestratorService.messaging.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;

@Service
public class GerentesProducer {

  @Autowired
  private RabbitTemplate template;

  public MessageWrapper<GerentesDto.Gerente> enviarMenssagem(MessageWrapper<GerentesDto.Gerente> message) {
    var response = (MessageWrapper<GerentesDto.Gerente>) template.convertSendAndReceiveAsType(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.GERENTES_KEY,
        message,
        new ParameterizedTypeReference<MessageWrapper<GerentesDto.Gerente>>() {
        });

    if (response == null)
      System.out.println("error on enviarMenssagem from gerentesService");
    return response;

  }

};
