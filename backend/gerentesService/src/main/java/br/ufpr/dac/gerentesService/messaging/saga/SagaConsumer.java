package br.ufpr.dac.gerentesService.messaging.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.gerentesService.repository.GerenteRepository;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SagaConsumer {

  GerenteRepository repo;
  InsertGerenteHandler insertGerenteHandler;
  RollbackRemoveGerenteHandler removeGerenteHandler;

  @Transactional
  @RabbitListener(queues = RabbitmqConsts.CONTAS_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {

    switch (message.getOperation()) {
      case SagaOperations.InsertGerente.INSERIR_NOVO -> {
        insertGerenteHandler.handleInsertGerente(
            SagaMessageWrapper.convertWrapper(message, GerentesDto.Gerente.class));
      }
      case SagaOperations.InsertGerente.ROLLBACK_REMOVER_GERENTE -> {
        removeGerenteHandler.handleRemoveGerente(
            SagaMessageWrapper.convertWrapper(message, Long.class));
      }
    }

  }

}
