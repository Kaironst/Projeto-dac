package br.ufpr.dac.orchestratorService.saga.clientesSaga.atualizarLimite;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.saga.SagaProducerFactory;
import br.ufpr.dac.orchestratorService.saga.SagaState;
import br.ufpr.dac.orchestratorService.saga.SagaStatus;
import br.ufpr.dac.orchestratorService.saga.SagaProducerFactory.SagaProducer;
import br.ufpr.dac.orchestratorService.saga.clientesSaga.autocadastro.AutocadastroPasso;
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations.AtualizarLimite;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor
public class AtualizarLimiteOrchestration {

  @Getter
  private final Map<UUID, SagaState<AtualizarLimiteData>> sagas = new ConcurrentHashMap<>();
  private final SagaProducerFactory producerFactory;
  private final Set<String> errors = Set.of(
      AtualizarLimite.ATUALIZAR_CLIENTE_ERROR,
      AtualizarLimite.ATUALIZAR_CONTA_ERROR,
      AtualizarLimite.ROLLBACK_RESTAURAR_CLIENTE_ERROR,
      MessageOperations.ERROR_GENERIC);

  public void startSaga(SagaMessageWrapper<UsersDto.Cliente> message) {
    System.out.println("atualizarLimite StartSaga acionado");
    UUID correlationId = UUID.randomUUID();
    message.setCorrelationId(correlationId);

    var state = new SagaState<AtualizarLimiteData>(
        correlationId,
        AtualizarLimitePasso.ATUALIZANDO_CLIENTE,
        SagaStatus.RUNNING,
        new AtualizarLimiteData());

    state.getSagaData().setClienteAtualizado(message.getData().getFirst());
    sagas.put(correlationId, state);

    // PASSO 1, ATUALIZAR CLIENTE PARA CÁLCULO DO LIMITE
    SagaProducer<UsersDto.Cliente> clienteMessageProducer = producerFactory.create();
    clienteMessageProducer.enviarMenssagem(
        new SagaMessageWrapper<UsersDto.Cliente>(
            SagaOperations.AtualizarLimite.ATUALIZAR_CLIENTE,
            List.of(state.getSagaData().getClienteAtualizado()),
            correlationId),
        RabbitmqConsts.USERS_SAGA_KEY);
  }

  // PASSO 2 ATUALIZAR LIMITE DA CONTA DO CLIENTE
  public void handleClienteAtualizado(SagaMessageWrapper<UsersDto.Cliente> message) {
    System.out.println("atualizarConta acionado");
    var state = sagas.get(message.getCorrelationId());

    // falha no primeiro passo - nada fo inserido
    if (errors.contains(message.getOperation())) {
      // handleRollback(state);
      return;
    }

    state.setStep(AtualizarLimitePasso.ATUALIZANDO_LIMITE);

    // resultado do passo anterior deve ser uma lista de cliente antigo - cliente
    // novo
    state.getSagaData().setClienteAntigo(message.getData().getFirst());

    // manda a lista em 2 valores: idCliente, salário
    SagaProducer<Double> doubleMessageProducer = producerFactory.create();
    doubleMessageProducer.enviarMenssagem(
        new SagaMessageWrapper<Double>(
            SagaOperations.AtualizarLimite.ATUALIZAR_CONTA,
            List.of(
                state.getSagaData().getClienteAtualizado().getSalario(),
                (double) state.getSagaData().getClienteAtualizado().getId()),
            state.getCorrelationId()),
        RabbitmqConsts.CONTAS_SAGA_KEY);
  }

  // finalizando
  public void handleContaAtualizada(SagaMessageWrapper<Long> message) {
    System.out.println("finalizar acionado");
    var state = sagas.get(message.getCorrelationId());

    // falha na atualização da conta, restaurar cliente
    if (errors.contains(message.getOperation())) {
      // handleRollback(state)
      return;
    }

    state.setStep(AtualizarLimitePasso.CONCLUINDO);
    state.setStatus(SagaStatus.SUCCESS);

    // cleanup
    sagas.remove(message.getCorrelationId());
  }

  public void handleRollback(SagaState<AtualizarLimiteData> state) {
    System.out.println("rollback acionado");

    state.setStatus(SagaStatus.COMPENSATING);

    switch (state.getStep()) {
      // falha a atualizar limite, restaurando cliente antigo
      case AtualizarLimitePasso.ATUALIZANDO_LIMITE:
        SagaProducer<UsersDto.Cliente> clienteProducer = producerFactory.create();
        clienteProducer.enviarMenssagem(
            new SagaMessageWrapper<UsersDto.Cliente>(
                SagaOperations.AtualizarLimite.ROLLBACK_RESTAURAR_CLIENTE,
                List.of(state.getSagaData().getClienteAntigo()),
                state.getCorrelationId()),
            RabbitmqConsts.USERS_SAGA_QUEUE);
        // cliente não atualizado, nada a se fazer
      case AtualizarLimitePasso.ATUALIZANDO_CLIENTE:
        break;
      default:
        break;
    }

    state.setStep(AtualizarLimitePasso.FINZALIZADO);
    state.setStatus(SagaStatus.FAILED);
    // cleanup
    sagas.remove(state.getCorrelationId());
  }

}
