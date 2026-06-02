package br.ufpr.dac.cqrsService.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MessageConsumer {

  @RabbitListener(queues = RabbitmqConsts.CQRS_QUEUE)
  public void recieve(String messageJson) {
    try {

      // TODO: colocar integração com debezium aqui

    } catch (Exception e) {
      System.out.println("error on message consumer listener");
      e.printStackTrace();
    }
  }

}
