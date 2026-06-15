package br.ufpr.dac.contasService.messaging.saga.autocadastro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
public class AutocadastroGetIdGerenteComMenosContasHandler {

  private RabbitTemplate template;
  private ContaRepository repo;

  @Transactional(readOnly = true)
  public void handleGetIdGerenteComMenosContas(SagaMessageWrapper<Long> message) {

    Long gerenteEscolhido = null;
    boolean sucesso = true;

    try {
      Map<Long, Long> contasPorGerente = repo.findAll().stream()
          .filter(c -> c.getGerente() != null)
          .collect(Collectors.groupingBy(Conta::getGerente, Collectors.counting()));

      Long menorQuantia = Collections.min(contasPorGerente.values());

      List<Long> gerentesComMenosContas = contasPorGerente.entrySet().stream()
          .filter(e -> e.getValue() == menorQuantia)
          .map(Map.Entry::getKey)
          .collect(Collectors.toList());

      gerenteEscolhido = gerentesComMenosContas.size() > 1
          ? gerentesComMenosContas.get(
              (int) Math.floor(Math.random() * gerentesComMenosContas.size()))
          : gerentesComMenosContas.getFirst();

    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }

    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.Autocadastro.GET_GERENTE_MENOS_CONTAS_RESULT
                : SagaOperations.Autocadastro.GET_GERENTE_MENOS_CONTAS_ERROR,
            sucesso ? List.of(gerenteEscolhido)
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
