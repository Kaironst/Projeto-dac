package br.ufpr.dac.orchestratorService.saga;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes.InsertGerentesOrchestration;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class SagaConsumer {

  private final InsertGerentesOrchestration insertGerentesOrchestration;

  private <T> List<T> convertList(List<Object> original, Class<T> tipo) {
    ObjectMapper mapper = new ObjectMapper();
    return original.stream()
        .map(obj -> mapper.convertValue(obj, tipo))
        .collect(Collectors.toList());
  }

  private <T> SagaMessageWrapper<T> convertWrapper(SagaMessageWrapper<Object> original, Class<T> tipo) {
    return new SagaMessageWrapper<T>(
        original.getOperation(),
        convertList(original.getData(), tipo),
        original.getCorrelationId());
  }

  @RabbitListener(queues = RabbitmqConsts.ORCHESTRATOR_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {
    try {

      switch (message.getOperation()) {

        // para a saga inserir gerente
        // ====================================================================
        case SagaOperations.InsertGerente.START -> {
          insertGerentesOrchestration.StartSaga(convertWrapper(message, GerentesDto.Gerente.class));
        }
        case SagaOperations.InsertGerente.GET_COM_MAIS_CONTAS_RESULT -> {
          insertGerentesOrchestration.handleInserirGerente(convertWrapper(message, Long.class));
        }
        case SagaOperations.InsertGerente.INSERIR_NOVO_RESULT -> {
          insertGerentesOrchestration.handleMoverContas(message);
        }
        case SagaOperations.InsertGerente.MOVER_CONTAS_RESULT -> {
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
