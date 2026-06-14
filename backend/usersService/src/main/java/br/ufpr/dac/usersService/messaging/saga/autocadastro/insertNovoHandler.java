package br.ufpr.dac.usersService.messaging.saga.autocadastro;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.usersService.entity.Cliente;
import br.ufpr.dac.usersService.messaging.consumer.MessageConsumer;
import br.ufpr.dac.usersService.messaging.producer.OutboxProducer;
import br.ufpr.dac.usersService.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class insertNovoHandler {

  private final RabbitTemplate template;
  private final ClienteRepository repo;
  private final OutboxProducer outboxProducer;

  @Transactional
  public void handleInserirNovo(SagaMessageWrapper<UsersDto.Cliente> message) {

    final List<Cliente> queryResult = new ArrayList<>();
    var sucesso = true;
    try {
      queryResult.addAll(repo.saveAll(MessageConsumer.dtoToClientes(message.getData())));
      if (queryResult.isEmpty() || queryResult == null) {
        sucesso = false;
      } else {
        message.getData().forEach((cliente) -> {
          cliente.setId(
              queryResult.stream()
                  .filter((c) -> c.getCpf().equals(cliente.getCpf()))
                  .collect(Collectors.toList())
                  .getFirst().getId());
          outboxProducer.writeToOutbox("created", cliente);
        });
      }
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }
    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.Autocadastro.INSERIR_NOVO_RESULT
                : SagaOperations.Autocadastro.INSERIR_NOVO_ERROR,
            sucesso ? List.of(MessageConsumer.clientesToDto(queryResult).getFirst().getId())
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
