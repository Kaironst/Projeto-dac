package br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.saga.SagaProducerFactory;
import br.ufpr.dac.orchestratorService.saga.SagaState;
import br.ufpr.dac.orchestratorService.saga.SagaStatus;
import br.ufpr.dac.orchestratorService.saga.sagaStep;
import br.ufpr.dac.orchestratorService.saga.SagaProducerFactory.SagaProducer;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations.InsertGerente;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor
public class InsertGerentesOrchestration {

  @Getter
  private final Map<UUID, SagaState<InsertGerentesData>> sagas = new ConcurrentHashMap<>();
  private final SagaProducerFactory producerFactory;
  private final Set<String> errors = Set.of(
      InsertGerente.GET_COM_MAIS_CONTAS_ERROR,
      InsertGerente.INSERIR_NOVO_ERROR,
      InsertGerente.MOVER_CONTAS_ERROR,
      MessageOperations.ERROR_GENERIC);

  public void StartSaga(SagaMessageWrapper<GerentesDto.Gerente> message) {
    UUID correlationId = UUID.randomUUID();
    message.setCorrelationId(correlationId);

    var state = new SagaState<InsertGerentesData>(
        correlationId,
        InsertGerentesPasso.BUSCANDO_GERENTE_COM_MAIS_CONTAS,
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

    // se a falha ocorrer no primeiro passo, que é apenas um get, não há necessidade
    // de rollback
    if (errors.contains(message.getOperation())) {
      handleRollback(message.getCorrelationId());
      return;
    }

    var state = sagas.get(message.getCorrelationId());
    state.setStep(InsertGerentesPasso.INSERINDO_GERENTE);
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

    // falha no passo 2, ainda não há nada a ser alterado
    if (errors.contains(message.getOperation())) {
      handleRollback(message.getCorrelationId());
      return;
    }

    var state = sagas.get(message.getCorrelationId());
    state.setStep(InsertGerentesPasso.DANDO_CONTA_AO_NOVO_GERENTE);

    SagaProducer<Long> gerenteMessageProducer = producerFactory.create();
    gerenteMessageProducer.enviarMenssagem(new SagaMessageWrapper<Long>(
        SagaOperations.InsertGerente.MOVER_CONTAS,
        List.of(state.getSagaData().getIdGerenteAntigo(), state.getSagaData().getGerenteAInserir().getId()),
        message.getCorrelationId()),
        RabbitmqConsts.GERENTES_SAGA_KEY);

  }

  public void handleFinalizar(SagaMessageWrapper<Object> message) {

    // falha no passo 3, conta não alterada, mas precisamos excluir o gerente novo
    // do passo 2 completo
    if (errors.contains(message.getOperation())) {
      handleRollback(message.getCorrelationId());
      return;
    }

    var state = sagas.get(message.getCorrelationId());
    state.setStep(InsertGerentesPasso.CONCLUINDO);
    state.setStatus(SagaStatus.SUCCESS);

    // TODO: decidir oq fazer aqui
  }

  public void handleRollback(UUID correlationId) {
    var state = sagas.get(correlationId);
    state.setStatus(SagaStatus.COMPENSATING);
    // remover o gerente inserido (unico caso de rollback necessário)
    if (state.getStep() == InsertGerentesPasso.DANDO_CONTA_AO_NOVO_GERENTE) {
      SagaProducer<Long> gerenteMessageProducer = producerFactory.create();
      gerenteMessageProducer.enviarMenssagem(new SagaMessageWrapper<Long>(
          SagaOperations.InsertGerente.ROLLBACK_REMOVER_GERENTE,
          List.of(state.getSagaData().getGerenteAInserir().getId()),
          message.getCorrelationId()),
          RabbitmqConsts.GERENTES_SAGA_KEY);
    }
    state.setStep(InsertGerentesPasso.FINALIZADO);
    state.setStatus(SagaStatus.FAILED);
  }

}
