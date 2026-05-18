package br.ufpr.dac.authService.messaging;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import br.ufpr.dac.authService.service.AuthService;
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

  private final AuthService authService;
  private final RabbitTemplate template;
  private final ObjectMapper mapper;

  @RabbitListener(queues = RabbitmqConsts.AUTH_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {
    switch (message.getOperation()) {
      case SagaOperations.Autocadastro.CRIAR_AUTH -> handleCriarAuth(
          mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<AutocadastroDto.UsuarioAuth>>() {
              }));
      case SagaOperations.Autocadastro.ROLLBACK_AUTH -> handleRollbackAuth(
          mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<AutocadastroDto.UsuarioAuth>>() {
              }));
    }
  }

  private void handleCriarAuth(SagaMessageWrapper<AutocadastroDto.UsuarioAuth> message) {
    try {
      var usuario = authService.criarUsuario(message.getData().getFirst());
      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.UsuarioAuth>(
          SagaOperations.Autocadastro.CRIAR_AUTH_RESULT,
          List.of(usuario),
          message.getCorrelationId()));
    } catch (Exception e) {
      e.printStackTrace();
      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.UsuarioAuth>(
          SagaOperations.Autocadastro.CRIAR_AUTH_ERROR,
          List.of(),
          message.getCorrelationId()));
    }
  }

  private void handleRollbackAuth(SagaMessageWrapper<AutocadastroDto.UsuarioAuth> message) {
    try {
      if (message.getData() != null && !message.getData().isEmpty()) {
        authService.removerUsuario(message.getData().getFirst());
      }

      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.UsuarioAuth>(
          SagaOperations.Autocadastro.ROLLBACK_AUTH_RESULT,
          List.of(),
          message.getCorrelationId()));
    } catch (Exception e) {
      e.printStackTrace();
      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.UsuarioAuth>(
          SagaOperations.Autocadastro.ROLLBACK_AUTH_ERROR,
          List.of(),
          message.getCorrelationId()));
    }
  }

  private void enviarMensagem(SagaMessageWrapper<AutocadastroDto.UsuarioAuth> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

}
