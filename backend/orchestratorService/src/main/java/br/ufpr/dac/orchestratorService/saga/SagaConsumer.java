package br.ufpr.dac.orchestratorService.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes.InsertGerentesOrchestration;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations.InsertGerente;
import lombok.AllArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class SagaConsumer {

  private final InsertGerentesOrchestration insertGerentesOrchestration;
  private final ObjectMapper mapper;

  @RabbitListener(queues = RabbitmqConsts.ORCHESTRATOR_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {
    try {

      switch (message.getOperation()) {

        // para a saga inserir gerente
        // ====================================================================
        case InsertGerente.START -> {
          insertGerentesOrchestration
              .StartSaga(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<GerentesDto.Gerente>>() {
                  }));
        }
        case InsertGerente.GET_COM_MAIS_CONTAS_RESULT, InsertGerente.GET_COM_MAIS_CONTAS_ERROR -> {
          insertGerentesOrchestration
              .handleInserirGerente(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
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
