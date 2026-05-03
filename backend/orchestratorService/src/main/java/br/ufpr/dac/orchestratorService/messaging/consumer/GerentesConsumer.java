package br.ufpr.dac.orchestratorService.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.messaging.producer.GerentesProducer;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;

@Component
public class GerentesConsumer {

  @Autowired
  GerentesProducer producer;

  @RabbitListener(queues = RabbitmqConsts.ORCHESTRATOR_GERENTES_QUEUE)
  public MessageWrapper<GerentesDto.Gerente> recieveMessage(MessageWrapper<GerentesDto.Gerente> message) {
    try {
      System.out.println(message);
      MessageWrapper<GerentesDto.Gerente> response = producer.enviarMenssagem(message);

      if (response == null)
        System.out.println("error on gerentesConsumer");
      return response;

    } catch (Exception e) {
      System.out.println("error on gerentesConsumer");
      e.printStackTrace();
      return new MessageWrapper<GerentesDto.Gerente>(MessageOperations.ERROR_GENERIC, null);
    }
  }

}
