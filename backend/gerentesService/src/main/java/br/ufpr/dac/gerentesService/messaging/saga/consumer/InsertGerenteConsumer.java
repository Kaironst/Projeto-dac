package br.ufpr.dac.gerentesService.messaging.saga.consumer;

import java.util.List;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.gerentesService.entity.Gerente;
import br.ufpr.dac.gerentesService.messaging.consumer.MessageConsumer;
import br.ufpr.dac.gerentesService.messaging.saga.producer.InsertGerenteProducer;
import br.ufpr.dac.gerentesService.repository.GerenteRepository;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class InsertGerenteConsumer {

  GerenteRepository repo;
  InsertGerenteProducer producer;

  @Transactional
  @RabbitListener(queues = RabbitmqConsts.CONTAS_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<GerentesDto.Gerente> message) {
    List<Gerente> queryResult = repo.saveAll(MessageConsumer.dtoToGerentes(message.getData()));
    producer.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            SagaOperations.InsertGerente.INSERIR_NOVO_RESULT,
            List.of(MessageConsumer.gerentesToDto(queryResult).getFirst().getId()),
            message.getCorrelationId()));
  }

}
