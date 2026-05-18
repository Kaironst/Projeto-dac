package br.ufpr.dac.contasService.messaging.saga;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.contasService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.AutocadastroDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RollbackContaAutocadastroHandler {

  private final RabbitTemplate template;
  private final ContaRepository repo;

  @Transactional
  public void handleRollbackConta(SagaMessageWrapper<AutocadastroDto.ContaCriada> message) {
    try {
      if (message.getData() != null && !message.getData().isEmpty()) {
        var conta = message.getData().getFirst().getConta();
        if (conta != null && conta.getId() != null && repo.existsById(conta.getId())) {
          repo.deleteById(conta.getId());
        }
      }

      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.ContaCriada>(
          SagaOperations.Autocadastro.ROLLBACK_CONTA_RESULT,
          List.of(),
          message.getCorrelationId()));
    } catch (Exception e) {
      e.printStackTrace();
      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.ContaCriada>(
          SagaOperations.Autocadastro.ROLLBACK_CONTA_ERROR,
          List.of(),
          message.getCorrelationId()));
    }
  }

  private void enviarMensagem(SagaMessageWrapper<AutocadastroDto.ContaCriada> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

}
