package br.ufpr.dac.gerentesService.messaging.saga.producer;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.gerentesService.entity.Gerente;
import br.ufpr.dac.gerentesService.messaging.consumer.MessageConsumer;
import br.ufpr.dac.gerentesService.repository.GerenteRepository;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InsertGerenteHandler {

  private RabbitTemplate template;
  GerenteRepository repo;

  public void handleInsertGerente(SagaMessageWrapper<GerentesDto.Gerente> message) {
    List<Gerente> queryResult = repo.saveAll(MessageConsumer.dtoToGerentes(message.getData()));
    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            SagaOperations.InsertGerente.INSERIR_NOVO_RESULT,
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
