package br.ufpr.dac.usersService.messaging.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.shared.dto.AutocadastroDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class SagaConsumer {

  private final AutocadastroSagaHandler autocadastroSagaHandler;
  private final ObjectMapper mapper;

  @RabbitListener(queues = RabbitmqConsts.USERS_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {
    switch (message.getOperation()) {
      case SagaOperations.Autocadastro.VALIDAR_CPF -> autocadastroSagaHandler.handleValidarCpf(
          mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<AutocadastroDto.SolicitacaoEntrada>>() {
              }));
      case SagaOperations.Autocadastro.REGISTRAR_SOLICITACAO -> autocadastroSagaHandler.handleRegistrarSolicitacao(
          mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<AutocadastroDto.SolicitacaoEntrada>>() {
              }));
      case SagaOperations.Autocadastro.VINCULAR_GERENTE -> autocadastroSagaHandler.handleVincularGerente(
          mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<AutocadastroDto.Solicitacao>>() {
              }));
      case SagaOperations.Autocadastro.APROVAR_SOLICITACAO -> autocadastroSagaHandler.handleAprovarSolicitacao(
          mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<AutocadastroDto.Aprovacao>>() {
              }));
      case SagaOperations.Autocadastro.ROLLBACK_SOLICITACAO -> autocadastroSagaHandler.handleRollbackSolicitacao(
          mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<AutocadastroDto.Solicitacao>>() {
              }));
    }
  }

}
