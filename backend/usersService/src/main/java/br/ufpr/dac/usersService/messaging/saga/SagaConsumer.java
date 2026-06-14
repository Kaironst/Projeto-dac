package br.ufpr.dac.usersService.messaging.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.usersService.messaging.saga.atualizarLimite.AtualizarClienteHandler;
import br.ufpr.dac.usersService.messaging.saga.atualizarLimite.RollbackRestaurarCliente;
import br.ufpr.dac.usersService.messaging.saga.autocadastro.RollbackRemoverClienteNovo;
import br.ufpr.dac.usersService.messaging.saga.autocadastro.insertNovoHandler;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class SagaConsumer {

  private final ObjectMapper mapper;
  insertNovoHandler insertNovoHandler;
  RollbackRemoverClienteNovo rollbackRemoverClienteNovo;
  AtualizarClienteHandler atualizarClienteHandler;
  RollbackRestaurarCliente rollbackRestaurarCliente;

  @RabbitListener(queues = RabbitmqConsts.USERS_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {

    switch (message.getOperation()) {

      case SagaOperations.Autocadastro.INSERIR_NOVO -> {
        insertNovoHandler.handleInserirNovo(
            mapper.convertValue(
                message,
                new TypeReference<SagaMessageWrapper<UsersDto.Cliente>>() {
                }));
      }
      case SagaOperations.Autocadastro.ROLLBACK_REVERTER_INSERIR_NOVO -> {
        rollbackRemoverClienteNovo.handleRollbackRemoverClienteNovo(
            mapper.convertValue(
                message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
      case SagaOperations.AtualizarLimite.ATUALIZAR_CLIENTE -> {
        atualizarClienteHandler.handleAtualizarCliente(
            mapper.convertValue(
                message,
                new TypeReference<SagaMessageWrapper<UsersDto.Cliente>>() {
                }));
      }
      case SagaOperations.AtualizarLimite.ROLLBACK_RESTAURAR_CLIENTE -> {
        rollbackRestaurarCliente.handleRollbackRestaurarCliente(
            mapper.convertValue(
                message,
                new TypeReference<SagaMessageWrapper<UsersDto.Cliente>>() {
                }));
      }
    }
  }

}
