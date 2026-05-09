package br.ufpr.dac.contasService.messaging.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@AllArgsConstructor
@Component
public class SagaConsumer {

  private GetIdGerenteComMaisContasHandler getIdGerenteComMaisContasHandler;
  private MoverContasHandler moverContasHandler;
  private final ObjectMapper mapper;

  @RabbitListener(queues = RabbitmqConsts.CONTAS_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {

    switch (message.getOperation()) {
      case SagaOperations.InsertGerente.GET_COM_MAIS_CONTAS -> {
        getIdGerenteComMaisContasHandler.HandleGetIdGerenteComMaisContas(
            mapper.convertValue(message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
      case SagaOperations.InsertGerente.MOVER_CONTAS -> {
        moverContasHandler.handleMoverContas(
            mapper.convertValue(message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
    }
  }

}
