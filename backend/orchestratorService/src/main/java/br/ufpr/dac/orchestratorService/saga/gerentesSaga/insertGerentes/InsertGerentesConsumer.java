package br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.saga.SagaStatus;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class InsertGerentesConsumer {

  private final InsertGerentesOrchestration orchestration;

  private <T> List<T> convertList(List<Object> original, Class<T> tipo) {
    ObjectMapper mapper = new ObjectMapper();
    return original.stream()
        .map(obj -> mapper.convertValue(obj, tipo))
        .collect(Collectors.toList());
  }

  @RabbitListener(queues = RabbitmqConsts.CONTAS_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {
    try {

      switch (message.getOperation()) {
        case SagaOperations.InsertGerente.START -> {
          orchestration.StartSaga(
              new SagaMessageWrapper<GerentesDto.Gerente>(
                  message.getOperation(),
                  convertList(message.getData(), GerentesDto.Gerente.class),
                  message.getCorrelationId()));
        }

        case SagaOperations.InsertGerente.GET_COM_MAIS_CONTAS_RESULT -> {
          Long resultado = convertList(message.getData(), Long.class).getFirst();
          var state = orchestration.getSagas().get(message.getCorrelationId());
          state.setStep(InsertGerentesPasso.GERENTE_COM_MAIS_CLIENTES_BUSCADO);
          state.getSagaData().setIdGerenteAntigo(resultado);
          orchestration.handleInserirGerente(message.getCorrelationId());
        }

        case SagaOperations.InsertGerente.INSERIR_NOVO_RESULT -> {
          var state = orchestration.getSagas().get(message.getCorrelationId());
          state.setStep(InsertGerentesPasso.GERENTE_INSERIDO);
          orchestration.handleMoverContas(message.getCorrelationId());
        }

        case SagaOperations.InsertGerente.MOVER_CONTAS_RESULT -> {
          var state = orchestration.getSagas().get(message.getCorrelationId());
          state.setStep(InsertGerentesPasso.CONTA_DADA_AO_NOVO_GERENTE);
          state.setStatus(SagaStatus.SUCCESS);
          orchestration.handleFinalizar(message.getCorrelationId());
        }

      }

    } catch (Exception e) {
      System.out.println("error on gerentesConsumer");
      e.printStackTrace();
    }
  }
}
