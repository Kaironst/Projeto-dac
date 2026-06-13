package br.ufpr.dac.contasService.messaging.saga.removerGerente;

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
  public void handleMoverContasInsert(SagaMessageWrapper<Long> message) {
    Conta contaEscolhida = null;
    boolean sucesso = true;
    try {
      List<Conta> contasGerenteAntigo = repo.findAllByGerente(message.getData().getFirst());
      if (contasGerenteAntigo != null && !contasGerenteAntigo.isEmpty()) {
        contaEscolhida = contasGerenteAntigo.get(new Random().nextInt(contasGerenteAntigo.size()));
        contaEscolhida.setGerente(message.getData().getLast());
        repo.save(contaEscolhida);
      } else {
        sucesso = false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }
    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.InsertGerente.MOVER_CONTAS_RESULT
                : SagaOperations.InsertGerente.MOVER_CONTAS_ERROR,
            contaEscolhida != null ? List.of(contaEscolhida.getId()) : List.of(),
            message.getCorrelationId()));
  }

  @Transactional
  public void handleMoverContasRemove(SagaMessageWrapper<Long> message) {
    boolean sucesso = true;
    List<Long> contasMovidasIds = new java.util.ArrayList<>();
    try {
      Long idGerenteARemover = message.getData().getFirst();
      Long idGerenteDestino = message.getData().getLast();
      List<Conta> contasGerenteAntigo = repo.findAllByGerente(idGerenteARemover);
      for (Conta conta : contasGerenteAntigo) {
        conta.setGerente(idGerenteDestino);
        contasMovidasIds.add(conta.getId());
      }
      repo.saveAll(contasGerenteAntigo);
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }
    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.RemoveGerente.MOVER_CONTAS_RESULT
                : SagaOperations.RemoveGerente.MOVER_CONTAS_ERROR,
            contasMovidasIds,
            message.getCorrelationId()));
  }

  @Transactional
  public void handleRollbackMoverContasRemove(SagaMessageWrapper<Long> message) {
    boolean sucesso = true;
    try {
      if (message.getData() != null && message.getData().size() > 2) {
        Long idGerenteDestino = message.getData().get(0);
        Long idGerenteRemovido = message.getData().get(1);
        List<Long> contasIds = message.getData().subList(2, message.getData().size());
        List<Conta> contas = repo.findAllById(contasIds);
        for (Conta conta : contas) {
          conta.setGerente(idGerenteRemovido);
        }
        repo.saveAll(contas);
      }
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }
    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.RemoveGerente.ROLLBACK_REVERTER_MOVER_CONTAS_RESULT
                : SagaOperations.RemoveGerente.ROLLBACK_REVERTER_MOVER_CONTAS_ERROR,
            List.of(),
            message.getCorrelationId()));
  }

  public void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

};
