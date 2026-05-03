package br.ufpr.dac.orchestratorService.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes.InsertGerentesOrchestration;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations.InsertGerente;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SagaConsumer {

  private final InsertGerentesOrchestration insertGerentesOrchestration;

  @RabbitListener(queues = RabbitmqConsts.ORCHESTRATOR_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {
    try {

      switch (message.getOperation()) {

        // para a saga inserir gerente
        // ====================================================================
        case InsertGerente.START -> {
          insertGerentesOrchestration.StartSaga(SagaMessageWrapper.convertWrapper(message, GerentesDto.Gerente.class));
        }
        case InsertGerente.GET_COM_MAIS_CONTAS_RESULT, InsertGerente.GET_COM_MAIS_CONTAS_ERROR -> {
          insertGerentesOrchestration.handleInserirGerente(SagaMessageWrapper.convertWrapper(message, Long.class));
        }
        case InsertGerente.INSERIR_NOVO_RESULT, InsertGerente.INSERIR_NOVO_ERROR -> {
          insertGerentesOrchestration.handleMoverContas(message);
        }
        case InsertGerente.MOVER_CONTAS_RESULT, InsertGerente.MOVER_CONTAS_ERROR -> {
          insertGerentesOrchestration.handleFinalizar(message);
        }
        // ===================================================================

      }

    } catch (Exception e) {
      System.out.println("error on sagaConsumer");
      e.printStackTrace();
    }
  }
}
