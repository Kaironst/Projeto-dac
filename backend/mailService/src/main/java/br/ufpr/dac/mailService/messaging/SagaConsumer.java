package br.ufpr.dac.mailService.messaging;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import br.ufpr.dac.mailService.service.MailService;
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

  private final MailService mailService;
  private final RabbitTemplate template;
  private final ObjectMapper mapper;

  @RabbitListener(queues = RabbitmqConsts.MAIL_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {
    switch (message.getOperation()) {
      case SagaOperations.Autocadastro.ENVIAR_EMAIL_APROVACAO -> handleEmail(
          mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<AutocadastroDto.Notificacao>>() {
              }),
          SagaOperations.Autocadastro.ENVIAR_EMAIL_APROVACAO_RESULT,
          SagaOperations.Autocadastro.ENVIAR_EMAIL_APROVACAO_ERROR);
      case SagaOperations.Autocadastro.ENVIAR_EMAIL_REJEICAO -> handleEmail(
          mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<AutocadastroDto.Notificacao>>() {
              }),
          SagaOperations.Autocadastro.ENVIAR_EMAIL_REJEICAO_RESULT,
          SagaOperations.Autocadastro.ENVIAR_EMAIL_REJEICAO_ERROR);
      case SagaOperations.Autocadastro.ENVIAR_EMAIL_FALHA -> handleEmail(
          mapper.convertValue(
              message,
              new TypeReference<SagaMessageWrapper<AutocadastroDto.Notificacao>>() {
              }),
          SagaOperations.Autocadastro.ENVIAR_EMAIL_FALHA_RESULT,
          SagaOperations.Autocadastro.ENVIAR_EMAIL_FALHA_ERROR);
    }
  }

  private void handleEmail(
      SagaMessageWrapper<AutocadastroDto.Notificacao> message,
      String resultOperation,
      String errorOperation) {
    try {
      var notificacao = message.getData().getFirst();
      mailService.enviarEmail(
          notificacao.getDestinatario(),
          notificacao.getAssunto(),
          notificacao.getConteudoHtml());

      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Notificacao>(
          resultOperation,
          message.getData(),
          message.getCorrelationId()));
    } catch (Exception e) {
      e.printStackTrace();
      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Notificacao>(
          errorOperation,
          List.of(),
          message.getCorrelationId()));
    }
  }

  private void enviarMensagem(SagaMessageWrapper<AutocadastroDto.Notificacao> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

}
