package br.ufpr.dac.contasService.messaging.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class SagaConsumer {

  private GetIdGerenteComMaisContasHandler getIdGerenteComMaisContasHandler;
  private MoverContasHandler moverContasHandler;

  @RabbitListener(queues = RabbitmqConsts.CONTAS_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {

    switch (message.getOperation()) {
      case SagaOperations.InsertGerente.GET_COM_MAIS_CONTAS -> {
        getIdGerenteComMaisContasHandler.HandleGetIdGerenteComMaisContas(
            SagaMessageWrapper.convertWrapper(message, Long.class));
      }
      case SagaOperations.InsertGerente.MOVER_CONTAS -> {
        moverContasHandler.handleMoverContas(
            SagaMessageWrapper.convertWrapper(message, Long.class));
      }
    }
  }

}
