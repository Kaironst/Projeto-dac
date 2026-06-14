package br.ufpr.dac.usersService.messaging.saga.autocadastro;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.usersService.messaging.producer.OutboxProducer;
import br.ufpr.dac.usersService.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RollbackRemoverClienteNovo {

  private final RabbitTemplate template;
  private final ClienteRepository repo;
  private final OutboxProducer outboxProducer;

  @Transactional
  public void handleRollbackRemoverClienteNovo(SagaMessageWrapper<Long> message) {

    var sucesso = true;
    Long idRemovido = null;
    try {
      idRemovido = message.getData().getFirst();
      repo.deleteById(idRemovido);
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }

    if (sucesso)
      outboxProducer.writeToOutbox("deleted", UsersDto.Cliente.builder().id(idRemovido).build());

    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.Autocadastro.ROLLBACK_REVERTER_INSERIR_NOVO_RESULT
                : SagaOperations.Autocadastro.ROLLBACK_REVERTER_INSERIR_NOVO_ERROR,
            sucesso ? List.of(idRemovido)
                : List.of(),
            message.getCorrelationId()));
  }

  public void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

}
