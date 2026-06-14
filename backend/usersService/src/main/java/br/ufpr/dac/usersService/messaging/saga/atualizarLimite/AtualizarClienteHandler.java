package br.ufpr.dac.usersService.messaging.saga.atualizarLimite;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
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
import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@AllArgsConstructor
public class AtualizarClienteHandler {

  private final RabbitTemplate template;
  private final ClienteRepository repo;
  private final OutboxProducer outboxProducer;
  private final ObjectMapper objectMapper;

  public void handleAtualizarCliente(SagaMessageWrapper<UsersDto.Cliente> message) {

    var resultado = new ArrayList<Cliente>();
    var sucesso = true;
    try {

      var cliente = message.getData().getFirst();
      var clienteAtual = repo.findById(cliente.getId()).orElseThrow();
      // copia valor do cliente para rollback serializando e desserializando
      var clienteAntigo = clienteAtual.toBuilder().enderecos(
          clienteAtual.getEnderecos().stream()
              .map((e) -> e.toBuilder().cliente(null).build())
              .collect(Collectors.toList()))
          .build();

      clienteAtual.setNome(cliente.getNome());
      clienteAtual.setCpf(cliente.getCpf());
      clienteAtual.setEmail(cliente.getEmail());
      clienteAtual.setEstado(cliente.getEstado());
      clienteAtual.setTelefone(cliente.getTelefone());
      clienteAtual.setSalario(cliente.getSalario());
      clienteAtual.setEnderecos(MessageConsumer.dtoToClientes(List.of(cliente)).getFirst().getEnderecos());

      outboxProducer.writeToOutbox("updated", cliente);
      resultado.add(clienteAntigo);
      resultado.add(clienteAtual);
      repo.save(clienteAtual);

    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }

    this.enviarMenssagem(
        new SagaMessageWrapper<UsersDto.Cliente>(
            sucesso ? SagaOperations.AtualizarLimite.ATUALIZAR_CLIENTE_RESULT
                : SagaOperations.AtualizarLimite.ATUALIZAR_CLIENTE_ERROR,
            sucesso ? (MessageConsumer.clientesToDto(resultado))
                : List.of(),
            message.getCorrelationId()));

  }

  public void enviarMenssagem(SagaMessageWrapper<UsersDto.Cliente> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

}
