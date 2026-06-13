package br.ufpr.dac.orchestratorService.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.saga.clientesSaga.AutoCadastroOrchestration;
import br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes.InsertGerentesOrchestration;
import br.ufpr.dac.orchestratorService.saga.gerentesSaga.removeGerentes.RemoveGerentesOrchestration;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations.Autocadastro;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations.InsertGerente;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations.RemoveGerente;
import lombok.AllArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class SagaConsumer {

  private final InsertGerentesOrchestration insertGerentesOrchestration;
  private final RemoveGerentesOrchestration removeGerentesOrchestration;
  private final AutoCadastroOrchestration autoCadastroOrchestration;
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
              .handleGerenteFound(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
        }
        case InsertGerente.INSERIR_NOVO_RESULT, InsertGerente.INSERIR_NOVO_ERROR -> {
          insertGerentesOrchestration
              .handleGerenteInserted(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
        }
        case InsertGerente.MOVER_CONTAS_RESULT, InsertGerente.MOVER_CONTAS_ERROR -> {
          insertGerentesOrchestration.handleContasSwapped(message);
        }
        // ===================================================================

        // para a saga remover gerente
        // ====================================================================
        case RemoveGerente.START -> {
          removeGerentesOrchestration
              .StartSaga(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
        }
        case RemoveGerente.GET_TODOS_GERENTES_RESULT, RemoveGerente.GET_TODOS_GERENTES_ERROR -> {
          removeGerentesOrchestration
              .handleTodosGerentesFound(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
        }
        case RemoveGerente.GET_COM_MENOS_CONTAS_RESULT, RemoveGerente.GET_COM_MENOS_CONTAS_ERROR -> {
          removeGerentesOrchestration
              .handleGerenteFound(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
        }
        case RemoveGerente.MOVER_CONTAS_RESULT, RemoveGerente.MOVER_CONTAS_ERROR -> {
          removeGerentesOrchestration.handleContasSwapped(mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<Long>>() {
              }));
        }
        case RemoveGerente.REMOVER_GERENTE_RESULT, RemoveGerente.REMOVER_GERENTE_ERROR -> {
          removeGerentesOrchestration.handleGerenteRemoved(mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<Long>>() {
              }));
        }
        // ===================================================================

        // para a saga autocadastro
        // ====================================================================
        case Autocadastro.START -> {
          autoCadastroOrchestration
              .startSaga(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<UsersDto.Cliente>>() {
                  }));
        }
        case Autocadastro.INSERIR_NOVO_RESULT, Autocadastro.INSERIR_NOVO_ERROR -> {
          autoCadastroOrchestration
              .handleClienteInserted(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
        }
        case Autocadastro.GET_GERENTE_MENOS_CONTAS_RESULT, Autocadastro.GET_GERENTE_MENOS_CONTAS_ERROR -> {
          autoCadastroOrchestration
              .handleGerenteFound(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
        }
        case Autocadastro.CRIAR_CONTA_RESULT, Autocadastro.CRIAR_CONTA_ERROR -> {
          autoCadastroOrchestration
              .handleContaCriada(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
        }

      }

    } catch (Exception e) {
      System.out.println("error on sagaConsumer");
      e.printStackTrace();
    }
  }
}
