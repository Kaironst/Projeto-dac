package br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.saga.SagaProducerFactory;
import br.ufpr.dac.orchestratorService.saga.SagaState;
import br.ufpr.dac.orchestratorService.saga.SagaStatus;
import br.ufpr.dac.orchestratorService.saga.SagaProducerFactory.SagaProducer;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor
public class InsertGerentesOrchestration {

  @Getter
  private final Map<UUID, SagaState<InsertGerentesData>> sagas = new ConcurrentHashMap<>();
  private final SagaProducerFactory producerFactory;

  public void StartSaga(SagaMessageWrapper<GerentesDto.Gerente> message) {
    UUID correlationId = UUID.randomUUID();
    message.setCorrelationId(correlationId);

    var state = new SagaState<InsertGerentesData>(
        correlationId,
        InsertGerentesPasso.INICIO,
        SagaStatus.RUNNING,
        new InsertGerentesData());

    state.getSagaData().setGerenteAInserir(message.getData().getFirst());
    sagas.put(correlationId, state);

    // PASSO 1, BUSCAR GERENTE COM MAIS NÚMERO DE CONTAS
    SagaProducer<Long> longMessageProducer = producerFactory.create();
    longMessageProducer.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            SagaOperations.InsertGerente.GET_COM_MAIS_CONTAS,
            List.of(),
            correlationId),
        RabbitmqConsts.CONTAS_SAGA_KEY);

  }

  // PASSO 2, INSERIR GERENTE NO BANCO DE DADOS
  public void handleInserirGerente(SagaMessageWrapper<Long> message) {

    var state = sagas.get(message.getCorrelationId());
    state.setStep(InsertGerentesPasso.GERENTE_COM_MAIS_CLIENTES_BUSCADO);
    state.getSagaData().setIdGerenteAntigo(message.getData().getFirst());

    SagaProducer<GerentesDto.Gerente> gerenteMessageProducer = producerFactory.create();
    gerenteMessageProducer.enviarMenssagem(new SagaMessageWrapper<GerentesDto.Gerente>(
        SagaOperations.InsertGerente.INSERIR_NOVO,
        List.of(state.getSagaData().getGerenteAInserir()),
        message.getCorrelationId()),
        RabbitmqConsts.GERENTES_SAGA_KEY);
  }

  // PASSSO 3, MOVER CONTA DO GERENTE ANTIGO AO NOVO
  public void handleMoverContas(SagaMessageWrapper<Object> message) {
    var state = sagas.get(message.getCorrelationId());

    state.setStep(InsertGerentesPasso.GERENTE_INSERIDO);

    SagaProducer<Long> gerenteMessageProducer = producerFactory.create();
    gerenteMessageProducer.enviarMenssagem(new SagaMessageWrapper<Long>(
        SagaOperations.InsertGerente.MOVER_CONTAS,
        List.of(state.getSagaData().getIdGerenteAntigo(), state.getSagaData().getGerenteAInserir().getId()),
        message.getCorrelationId()),
        RabbitmqConsts.GERENTES_SAGA_KEY);

  }

  public void handleFinalizar(SagaMessageWrapper<Object> message) {
    var state = sagas.get(message.getCorrelationId());
    state.setStep(InsertGerentesPasso.CONTA_DADA_AO_NOVO_GERENTE);
    state.setStatus(SagaStatus.SUCCESS);

    // TODO: decidir oq fazer aqui
  }

}
