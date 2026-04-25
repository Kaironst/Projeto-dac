package br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.saga.SagaProducer;
import br.ufpr.dac.orchestratorService.saga.SagaState;
import br.ufpr.dac.orchestratorService.saga.SagaStatus;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class InsertGerentesOrchestration {

  private final Map<UUID, SagaState> sagas = new ConcurrentHashMap<>();
  private final SagaProducer<Long> longMessageProducer;

  public UUID StartSaga(SagaMessageWrapper<Long> request) {
    UUID correlationId = UUID.randomUUID();
    request.setCorrelationId(correlationId);
    var state = new SagaState(correlationId, InsertGerentesPasso.INICIO, SagaStatus.RUNNING);
    sagas.put(correlationId, state);

    // PASSO 1, BUSCAR GERENTE COM MAIS NÚMERO DE CONTAS
    // TODO: criar listener para esse negócio
    longMessageProducer.enviarMenssagem(new SagaMessageWrapper<Long>(), RabbitmqConsts.CONTAS_SAGA_KEY);

    return correlationId;

  }

}
