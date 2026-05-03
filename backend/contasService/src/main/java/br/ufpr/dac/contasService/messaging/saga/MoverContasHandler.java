package br.ufpr.dac.contasService.messaging.saga;

import java.util.List;
import java.util.Random;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.contasService.entity.Conta;
import br.ufpr.dac.contasService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MoverContasHandler {

  private RabbitTemplate template;
  private ContaRepository repo;

  @Transactional
  public void handleMoverContas(SagaMessageWrapper<Long> message) {

    Conta contaEscolhida = null;
    boolean sucesso = true;
    try {
      List<Conta> contasGerenteAntigo = repo.findAllByGerente(message.getData().getFirst());
      contaEscolhida = contasGerenteAntigo.get(new Random().nextInt(contasGerenteAntigo.size()));

      contaEscolhida.setGerente(message.getData().getLast());
      repo.save(contaEscolhida);
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }

    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.InsertGerente.MOVER_CONTAS_RESULT
                : SagaOperations.InsertGerente.MOVER_CONTAS_ERROR,
            List.of(contaEscolhida.getId()),
            message.getCorrelationId()));

  }

  public void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_QUEUE,
        message);
  }

};
