package br.ufpr.dac.orchestratorService.saga;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class SagaProducerFactory {

  private final RabbitTemplate template;

  public <T> SagaProducer<T> create() {
    return new SagaProducer<>(template);
  }

  @AllArgsConstructor
  public static class SagaProducer<T> {

    private final RabbitTemplate template;

    public void enviarMenssagem(SagaMessageWrapper<T> message, String routingKey) {
      template.convertAndSend(
          RabbitmqConsts.APP_EXCHANGE,
          routingKey,
          message);
    }

  }

}
