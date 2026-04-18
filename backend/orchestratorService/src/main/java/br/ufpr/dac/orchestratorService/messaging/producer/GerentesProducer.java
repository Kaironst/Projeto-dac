package br.ufpr.dac.orchestratorService.messaging.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;

@Service
public class GerentesProducer {

  @Autowired
  private RabbitTemplate template;

  public GerentesDto.Message enviarMenssagem(GerentesDto.Message message) {
    var response = (GerentesDto.Message) template.convertSendAndReceiveAsType(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.GERENTES_KEY,
        message,
        new ParameterizedTypeReference<GerentesDto.Message>() {
        });

    if (response == null)
      System.out.println("error on enviarMenssagem from gerentesService");
    return response == null ? new GerentesDto.Message(MessageOperations.ERROR_GENERIC, null) : response;

  }

};
