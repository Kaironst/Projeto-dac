package br.ufpr.dac.gerentesService.messaging.saga.removerGerente;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.gerentesService.entity.Gerente;
import br.ufpr.dac.gerentesService.repository.GerenteRepository;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import jakarta.transaction.Transactional;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GetAllGerentesHandler {

  private RabbitTemplate template;
  private GerenteRepository repo;

  @Transactional
  public void handleGetAllGerentes(SagaMessageWrapper<Long> message) {

    boolean sucesso = true;
    List<Long> gerentesIds = List.of();
    try {
      gerentesIds = repo.findAll().stream()
          .filter(g -> g.getAdministrador() == null || !g.getAdministrador())
          .map(Gerente::getId)
          .collect(Collectors.toList());
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }
    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.RemoveGerente.GET_TODOS_GERENTES_RESULT
                : SagaOperations.RemoveGerente.GET_TODOS_GERENTES_ERROR,
            gerentesIds,
            message.getCorrelationId()));
  }

  public void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }
}
