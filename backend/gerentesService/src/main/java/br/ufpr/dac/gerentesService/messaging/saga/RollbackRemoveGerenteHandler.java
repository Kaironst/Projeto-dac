package br.ufpr.dac.gerentesService.messaging.saga;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.gerentesService.repository.GerenteRepository;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import jakarta.transaction.Transactional;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RollbackRemoveGerenteHandler {

  private RabbitTemplate template;
  GerenteRepository repo;

  @Transactional
  public void handleRemoveGerente(SagaMessageWrapper<Long> message) {

    boolean sucesso = true;
    try {
      repo.deleteAllById(message.getData());
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }
    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.InsertGerente.ROLLBACK_REMOVER_GERENTE_RESULT
                : SagaOperations.InsertGerente.ROLLBACK_REMOVER_GERENTE_ERROR,
            List.of(),
            message.getCorrelationId()));
  }

  public void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

};
