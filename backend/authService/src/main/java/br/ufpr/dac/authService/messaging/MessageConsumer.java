package br.ufpr.dac.authService.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.authService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.ContasDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MessageConsumer {

  private final ContaRepository repo;

  @RabbitListener(queues = RabbitmqConsts.AUTH_QUEUE)
  public ResponseDto recieve(MessageWrapper<ContasDto.Conta> message) {

  }

}
