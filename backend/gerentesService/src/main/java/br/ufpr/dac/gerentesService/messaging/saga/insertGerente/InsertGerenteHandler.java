package br.ufpr.dac.gerentesService.messaging.saga.insertGerente;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.gerentesService.entity.Gerente;
import br.ufpr.dac.gerentesService.messaging.consumer.MessageConsumer;
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
public class InsertGerenteHandler {

  private RabbitTemplate template;
  GerenteRepository repo;
  OutboxProducer outboxProducer;

  @Transactional
  public void handleInsertGerente(SagaMessageWrapper<GerentesDto.Gerente> message) {

    final List<Gerente> queryResult = new ArrayList<>();
    boolean sucesso = true;
    try {
      queryResult.addAll(repo.saveAll(MessageConsumer.dtoToGerentes(message.getData())));
      if (queryResult.isEmpty() || queryResult == null) {
        sucesso = false;
      } else {
        message.getData().forEach((gerente) -> {
          gerente.setId(
              queryResult.stream()
                  .filter((g) -> g.getCpf().equals(gerente.getCpf()))
                  .collect(Collectors.toList())
                  .getFirst().getId());
          outboxProducer.writeToOutbox("created", gerente);
        });
      }
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }
    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.InsertGerente.INSERIR_NOVO_RESULT
                : SagaOperations.InsertGerente.INSERIR_NOVO_ERROR,
            sucesso ? List.of(MessageConsumer.gerentesToDto(queryResult).getFirst().getId())
                : List.of(),
            message.getCorrelationId()));
  }

  public void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

};
