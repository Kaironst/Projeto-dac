package br.ufpr.dac.gerentesService.messaging.saga.removerGerente;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.gerentesService.messaging.producer.OutboxProducer;
import br.ufpr.dac.gerentesService.repository.GerenteRepository;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import jakarta.transaction.Transactional;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RemoveGerenteHandler {

  private RabbitTemplate template;
  private GerenteRepository repo;
  private OutboxProducer outboxProducer;

  @Transactional
  public void handleRemoveGerente(SagaMessageWrapper<Long> message) {

    boolean sucesso = true;
    try {
      Long idARemover = message.getData().getFirst();
      long count = repo.count();
      if (count <= 1) {
        // "Não permitir a remoção do último gerente do banco."
        System.out.println("Remoção negada: não é possível remover o último gerente.");
        sucesso = false;
      } else {
        repo.deleteById(idARemover);
        outboxProducer.writeToOutbox("deleted", GerentesDto.Gerente.builder().id(idARemover).build());
      }
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }
    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.RemoveGerente.REMOVER_GERENTE_RESULT
                : SagaOperations.RemoveGerente.REMOVER_GERENTE_ERROR,
            List.of(),
            message.getCorrelationId()));
  }

  public void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }
}
