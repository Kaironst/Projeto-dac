package br.ufpr.dac.usersService.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.shared.dto.AutocadastroDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.usersService.messaging.saga.AutocadastroSagaHandler;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class AutocadastroQueryConsumer {

  private final AutocadastroSagaHandler autocadastroSagaHandler;

  @RabbitListener(queues = RabbitmqConsts.USERS_AUTOCADASTRO_QUEUE)
  public MessageWrapper<AutocadastroDto.Solicitacao> recieve(MessageWrapper<AutocadastroDto.Solicitacao> message) {
    try {
      switch (message.getOperation()) {
        case MessageOperations.READ_AUTOCADASTRO_PENDENTES -> {
          return autocadastroSagaHandler.handleListarPendentes();
        }
        default -> {
          throw new UnsupportedOperationException();
        }
      }
    } catch (Exception e) {
      System.out.println("error on autocadastro query consumer listener");
      e.printStackTrace();
      return new MessageWrapper<AutocadastroDto.Solicitacao>(MessageOperations.ERROR_GENERIC, null);
    }
  }

}
