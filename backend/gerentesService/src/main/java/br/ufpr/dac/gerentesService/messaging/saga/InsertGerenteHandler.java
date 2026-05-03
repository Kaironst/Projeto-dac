package br.ufpr.dac.gerentesService.messaging.saga;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.gerentesService.entity.Gerente;
import br.ufpr.dac.gerentesService.messaging.consumer.MessageConsumer;
import br.ufpr.dac.gerentesService.repository.GerenteRepository;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import jakarta.transaction.Transactional;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InsertGerenteHandler {

  private RabbitTemplate template;
  GerenteRepository repo;

  @Transactional
  public void handleInsertGerente(SagaMessageWrapper<GerentesDto.Gerente> message) {

    List<Gerente> queryResult = null;
    boolean sucesso = true;
    try {
      queryResult = repo.saveAll(MessageConsumer.dtoToGerentes(message.getData()));
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }
    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.InsertGerente.INSERIR_NOVO_RESULT
                : SagaOperations.InsertGerente.INSERIR_NOVO_ERROR,
            List.of(MessageConsumer.gerentesToDto(queryResult).getFirst().getId()),
            message.getCorrelationId()));
  }

  public void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

};
