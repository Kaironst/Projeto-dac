package br.ufpr.dac.orchestratorService.saga.gerentesSaga.removeGerentes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.saga.SagaProducerFactory;
import br.ufpr.dac.orchestratorService.saga.SagaProducerFactory.SagaProducer;
import br.ufpr.dac.orchestratorService.saga.SagaState;
import br.ufpr.dac.orchestratorService.saga.SagaStatus;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations.RemoveGerente;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor()
public class RemoveGerentesOrchestration {

  @Getter
  private final Map<UUID, SagaState<RemoveGerentesData>> sagas = new ConcurrentHashMap<>();
  private final SagaProducerFactory producerFactory;
  private final Set<String> errors = Set.of(
      RemoveGerente.MOVER_CONTAS_ERROR,
      RemoveGerente.GET_COM_MENOS_CONTAS_ERROR,
      RemoveGerente.REMOVER_GERENTE_ERROR,
      RemoveGerente.ROLLBACK_REVERTER_MOVER_CONTAS_ERROR,
      MessageOperations.ERROR_GENERIC);

  public void StartSaga(SagaMessageWrapper<Long> message) {
    System.out.println("RemoveGerente acionado");
    UUID correlationId = UUID.randomUUID();
    message.setCorrelationId(correlationId);

    var state = new SagaState<RemoveGerentesData>(
        correlationId,
        RemoveGerentesPasso.BUSCANDO_GERENTE_COM_MENOS_CONTAS,
        SagaStatus.RUNNING,
        new RemoveGerentesData());

    state.getSagaData().setIdGerenteARemover(message.getData().getFirst());
    sagas.put(correlationId, state);

    // PASSO 1, BUSCAR GERENTE COM MENOR NÚMERO DE CONTAS
    SagaProducer<Long> longMessageProducer = producerFactory.create();
    longMessageProducer.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            SagaOperations.RemoveGerente.GET_COM_MENOS_CONTAS,
            List.of(),
            correlationId),
        RabbitmqConsts.CONTAS_SAGA_KEY);

  }

  // PASSO 2, MOOVER CONTA DO GERENTE REMOVIDO A O COM MENOS CONTAS
  public void handleGerenteFound(SagaMessageWrapper<Long> message) {
    System.out.println("moverContas acionado");
    var state = sagas.get(message.getCorrelationId());

    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(RemoveGerentesPasso.DANDO_CONTAS_AO_GERENTE_COM_MENOS);
    state.getSagaData().setIdGerenteComMenosContas(message.getData().getFirst());

    SagaProducer<Long> longMessageProducer = producerFactory.create();
    longMessageProducer.enviarMenssagem(new SagaMessageWrapper<Long>(
        SagaOperations.RemoveGerente.MOVER_CONTAS,
        List.of(state.getSagaData().getIdGerenteARemover(), state.getSagaData().getIdGerenteComMenosContas()),
        message.getCorrelationId()),
        RabbitmqConsts.CONTAS_SAGA_KEY);

  }

  // PASSO 3, REMOVER GERENTE NO BANCO DE DADOS
  public void handleContasSwapped(SagaMessageWrapper<Object> message) {
    System.out.println("removerGerente acionado");
    var state = sagas.get(message.getCorrelationId());

    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(RemoveGerentesPasso.REMOVENDO_GERENTE);

    SagaProducer<Long> longMessageProducer = producerFactory.create();
    longMessageProducer.enviarMenssagem(new SagaMessageWrapper<Long>(
        SagaOperations.RemoveGerente.REMOVER_GERENTE,
        List.of(state.getSagaData().getIdGerenteARemover()),
        message.getCorrelationId()),
        RabbitmqConsts.GERENTES_SAGA_KEY);
  }

  // finalizar
  public void handleGerenteRemoved(SagaMessageWrapper<Object> message) {
    System.out.println("finalizar acionado");
    var state = sagas.get(message.getCorrelationId());

    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(RemoveGerentesPasso.FINALIZADO);
    state.setStatus(SagaStatus.SUCCESS);

    System.out.println("saga removerGerentes finalizada!");

    // TODO: mandar menssagem para o api gateway com o retorno da saga para
    // atualização dinâmica das páginas

    // cleanup
    sagas.remove(message.getCorrelationId());
  }

  public void handleRollback(SagaState<RemoveGerentesData> state) {
    state.setStatus(SagaStatus.COMPENSATING);
    SagaProducer<Long> longMessageProducer = producerFactory.create();
    switch (state.getStep()) {
      // gerente não removido, troca de volta as contas entre os 2
      case RemoveGerentesPasso.REMOVENDO_GERENTE:
        longMessageProducer.enviarMenssagem(new SagaMessageWrapper<Long>(
            SagaOperations.RemoveGerente.ROLLBACK_REVERTER_MOVER_CONTAS,
            List.of(state.getSagaData().getIdGerenteComMenosContas(), state.getSagaData().getIdGerenteARemover()),
            state.getCorrelationId()), RabbitmqConsts.CONTAS_SAGA_KEY);
        // troca falha, não há operações a serem revertidas
      case RemoveGerentesPasso.DANDO_CONTAS_AO_GERENTE_COM_MENOS:
        // apenas read
      case RemoveGerentesPasso.BUSCANDO_GERENTE_COM_MENOS_CONTAS:
        break;
      default:
    }

    state.setStep(RemoveGerentesPasso.FINALIZADO);
    state.setStatus(SagaStatus.FAILED);
    System.out.println("rollback completo!");
    // cleanup
    sagas.remove(state.getCorrelationId());

  }

}
