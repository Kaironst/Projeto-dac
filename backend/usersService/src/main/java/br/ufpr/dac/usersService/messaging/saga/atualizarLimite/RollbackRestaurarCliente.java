package br.ufpr.dac.usersService.messaging.saga.atualizarLimite;

import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.usersService.messaging.consumer.MessageConsumer;
import br.ufpr.dac.usersService.messaging.producer.OutboxProducer;
import br.ufpr.dac.usersService.repository.ClienteRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RollbackRestaurarCliente {

  private final RabbitTemplate template;
  private final ClienteRepository repo;
  private final OutboxProducer outboxProducer;

  public void handleRollbackRestaurarCliente(SagaMessageWrapper<UsersDto.Cliente> message) {

    var sucesso = true;
    try {

      var clienteAntigo = message.getData().getFirst();
      var clienteAtual = repo.findById(clienteAntigo.getId()).orElseThrow();
      // copia valor do cliente para rollback serializando e desserializando

      clienteAtual.setNome(clienteAntigo.getNome());
      clienteAtual.setCpf(clienteAntigo.getCpf());
      clienteAtual.setEmail(clienteAntigo.getEmail());
      clienteAtual.setEstado(clienteAntigo.getEstado());
      clienteAtual.setTelefone(clienteAntigo.getTelefone());
      clienteAtual.setSalario(clienteAntigo.getSalario());
      clienteAtual.setEnderecos(MessageConsumer.dtoToClientes(List.of(clienteAntigo)).getFirst().getEnderecos());

      outboxProducer.writeToOutbox("updated", clienteAntigo);
      repo.save(clienteAtual);

    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }

    this.enviarMenssagem(
        new SagaMessageWrapper<UsersDto.Cliente>(
            sucesso ? SagaOperations.AtualizarLimite.ROLLBACK_RESTAURAR_CLIENTE_RESULT
                : SagaOperations.AtualizarLimite.ROLLBACK_RESTAURAR_CLIENTE_ERROR,
            sucesso ? (message.getData())
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
