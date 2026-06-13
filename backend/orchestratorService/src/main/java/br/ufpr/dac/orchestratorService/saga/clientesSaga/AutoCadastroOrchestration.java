package br.ufpr.dac.orchestratorService.saga.clientesSaga;

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
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations.Autocadastro;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor
public class AutoCadastroOrchestration {

  @Getter
  private final Map<UUID, SagaState<AutocadastroData>> sagas = new ConcurrentHashMap<>();
  private final SagaProducerFactory producerFactory;
  private final Set<String> errors = Set.of(
      Autocadastro.INSERIR_NOVO_ERROR,
      Autocadastro.GET_GERENTE_MENOS_CONTAS_ERROR,
      Autocadastro.CRIAR_CONTA_ERROR,
      Autocadastro.ROLLBACK_REVERTER_INSERIR_NOVO_ERROR,
      MessageOperations.ERROR_GENERIC);

  public void startSaga(SagaMessageWrapper<UsersDto.Cliente> message) {
    System.out.println("autocadastro StartSaga acionado");
    UUID correlationId = UUID.randomUUID();
    message.setCorrelationId(correlationId);

    var state = new SagaState<AutocadastroData>(
        correlationId,
        AutocadastroPasso.SALVANDO_CLIENTE,
        SagaStatus.RUNNING,
        new AutocadastroData());

    state.getSagaData().setClienteAInserir(message.getData().getFirst());
    sagas.put(correlationId, state);

    // PASSO 1, INSERIR NOVO CLIENTE
    SagaProducer<UsersDto.Cliente> clienteMessageProducer = producerFactory.create();
    clienteMessageProducer.enviarMenssagem(
        new SagaMessageWrapper<UsersDto.Cliente>(
            SagaOperations.Autocadastro.INSERIR_NOVO,
            List.of(state.getSagaData().getClienteAInserir()),
            correlationId),
        RabbitmqConsts.USERS_SAGA_KEY);
  }

  // PASSO 2, BUSCAR GERENTE COM MENOS CONTAS
  public void handleClienteInserted(SagaMessageWrapper<Long> message) {
    System.out.println("buscarGerenteComMenosContas acionado");
    var state = sagas.get(message.getCorrelationId());

    // falha no primeiro passo - nada foi inserido
    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(AutocadastroPasso.BUSCANDO_GERENTE_COM_MENOS_CONTAS);

    // lida com resultado da saga anterior
    state.getSagaData().setIdClienteInserido(message.getData().getFirst());

    SagaProducer<Long> longMessageProducer = producerFactory.create();
    longMessageProducer.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            SagaOperations.Autocadastro.GET_GERENTE_MENOS_CONTAS,
            List.of(),
            message.getCorrelationId()),
        RabbitmqConsts.CONTAS_SAGA_KEY);
  }

  // PASSO 3, INSERIR CONTA
  public void handleGerenteFound(SagaMessageWrapper<Long> message) {
    System.out.println("inserirConta acionado");
    var state = sagas.get(message.getCorrelationId());

    // falha no segundo passo - reverter inserção do cliente
    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(AutocadastroPasso.CRIANDO_CONTA);

    // atualiza gerente no estado pela saga anterior
    state.getSagaData().setGerenteComMenosClientesId(message.getData().getFirst());

    // manda a lista em 3 valores idCliente, idGerente, salárioCliente
    SagaProducer<Double> doubleMessageProducer = producerFactory.create();
    doubleMessageProducer.enviarMenssagem(new SagaMessageWrapper<Double>(
        SagaOperations.Autocadastro.CRIAR_CONTA,
        // cast como double para conseguir carregar tudo no mesmo payload
        List.of(
            (double) state.getSagaData().getIdClienteInserido(),
            (double) state.getSagaData().getGerenteComMenosClientesId(),
            state.getSagaData().getClienteAInserir().getSalario()),
        state.getCorrelationId()),
        RabbitmqConsts.CONTAS_SAGA_KEY);

  }

  // finalizando
  public void handleContaCriada(SagaMessageWrapper<Long> message) {
    System.out.println("finalizar acionado");
    var state = sagas.get(message.getCorrelationId());

    // falha na inserção da conta, remover cliente
    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(AutocadastroPasso.CONCLUINDO);
    state.setStatus(SagaStatus.SUCCESS);

    // cleanup
    sagas.remove(message.getCorrelationId());

  }

  public void handleRollback(SagaState<AutocadastroData> state) {
    System.out.println("rollback acionado");

    state.setStatus(SagaStatus.COMPENSATING);

    switch (state.getStep()) {
      // falha ao criar conta - remover cliente
      case AutocadastroPasso.CRIANDO_CONTA:
        // leitura falha, remover cliente
      case AutocadastroPasso.BUSCANDO_GERENTE_COM_MENOS_CONTAS:
        SagaProducer<Long> longMessageProducer = producerFactory.create();
        longMessageProducer.enviarMenssagem(new SagaMessageWrapper<Long>(
            SagaOperations.Autocadastro.ROLLBACK_REVERTER_INSERIR_NOVO,
            List.of(state.getSagaData().getIdClienteInserido()),
            state.getCorrelationId()),
            RabbitmqConsts.USERS_SAGA_KEY);

        // conta não inserida, sem nada a fazer
      case AutocadastroPasso.SALVANDO_CLIENTE:
        break;
      default:
        break;
    }
  }

}
