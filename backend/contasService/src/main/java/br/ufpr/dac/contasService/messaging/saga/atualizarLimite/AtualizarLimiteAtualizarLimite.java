package br.ufpr.dac.contasService.messaging.saga.atualizarLimite;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.contasService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AtualizarLimiteAtualizarLimite {

  private final RabbitTemplate template;
  private final ContaRepository repo;

  public void handleAtualizarLimite(SagaMessageWrapper<Double> message) {

    System.out.println("handleAtualizarLimite triggered");

    Long contaId = null;
    boolean sucesso = true;
    try {
      // pega os valores enviados da lista
      Double salario = message.getData().get(0);
      Long ClienteId = message.getData().get(1).longValue();

      var conta = repo.findAllByCliente(ClienteId).getFirst();
      contaId = conta.getId();
      conta.setLimite(salario / 2);
      repo.save(conta);

    } catch (Exception e) {
      sucesso = false;
      e.printStackTrace();
    }

    this.enviarMenssagem(new SagaMessageWrapper<Long>(
        sucesso ? SagaOperations.AtualizarLimite.ATUALIZAR_CONTA_RESULT
            : SagaOperations.AtualizarLimite.ATUALIZAR_CONTA_ERROR,
        sucesso ? List.of(contaId)
            : List.of(),
        message.getCorrelationId()));
    System.out.println("menssagem de retorno enviada");
  }

  private void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

}
