package br.ufpr.dac.contasService.messaging.saga;

import java.util.HashMap;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.contasService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EscolherGerenteComMenosClientesHandler {

  private final RabbitTemplate template;
  private final ContaRepository repo;

  @Transactional(readOnly = true)
  public void handleEscolherGerente(SagaMessageWrapper<Long> message) {
    try {
      var gerenteEscolhido = escolherGerente(message.getData());
      enviarMensagem(new SagaMessageWrapper<Long>(
          SagaOperations.Autocadastro.ESCOLHER_GERENTE_RESULT,
          List.of(gerenteEscolhido),
          message.getCorrelationId()));
    } catch (Exception e) {
      e.printStackTrace();
      enviarMensagem(new SagaMessageWrapper<Long>(
          SagaOperations.Autocadastro.ESCOLHER_GERENTE_ERROR,
          List.of(),
          message.getCorrelationId()));
    }
  }

  private Long escolherGerente(List<Long> gerentes) {
    if (gerentes == null || gerentes.isEmpty()) {
      throw new IllegalArgumentException("Nenhum gerente disponivel para autocadastro.");
    }

    var contasPorGerente = new HashMap<Long, Long>();
    gerentes.forEach(gerenteId -> contasPorGerente.put(gerenteId, 0L));

    repo.findAll().forEach(conta -> {
      if (contasPorGerente.containsKey(conta.getGerente())) {
        contasPorGerente.computeIfPresent(conta.getGerente(), (gerenteId, total) -> total + 1);
      }
    });

    return contasPorGerente.entrySet().stream()
        .sorted((a, b) -> {
          int comparacaoPorTotal = a.getValue().compareTo(b.getValue());
          if (comparacaoPorTotal != 0) {
            return comparacaoPorTotal;
          }
          return a.getKey().compareTo(b.getKey());
        })
        .findFirst()
        .orElseThrow()
        .getKey();
  }

  private void enviarMensagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

}
