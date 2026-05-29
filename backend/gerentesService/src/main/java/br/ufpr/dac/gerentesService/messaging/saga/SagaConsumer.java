package br.ufpr.dac.gerentesService.messaging.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.gerentesService.repository.GerenteRepository;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class SagaConsumer {

  GerenteRepository repo;
  InsertGerenteHandler insertGerenteHandler;
  RollbackRemoveGerenteHandler rollbackRemoveGerenteHandler;
  RemoveGerenteHandler removeGerenteHandler;
  GetAllGerentesHandler getAllGerentesHandler;
  private final ObjectMapper mapper;

  @RabbitListener(queues = RabbitmqConsts.GERENTES_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {

    switch (message.getOperation()) {
      case SagaOperations.InsertGerente.INSERIR_NOVO -> {
        insertGerenteHandler.handleInsertGerente(
            mapper.convertValue(
                message,
                new TypeReference<SagaMessageWrapper<GerentesDto.Gerente>>() {
                }));
      }
      case SagaOperations.InsertGerente.ROLLBACK_REMOVER_GERENTE -> {
        rollbackRemoveGerenteHandler.handleRemoveGerente(
            mapper.convertValue(
                message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
      case SagaOperations.RemoveGerente.GET_TODOS_GERENTES -> {
        getAllGerentesHandler.handleGetAllGerentes(
            mapper.convertValue(
                message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
      case SagaOperations.RemoveGerente.REMOVER_GERENTE -> {
        removeGerenteHandler.handleRemoveGerente(
            mapper.convertValue(
                message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
    }

  }

}
