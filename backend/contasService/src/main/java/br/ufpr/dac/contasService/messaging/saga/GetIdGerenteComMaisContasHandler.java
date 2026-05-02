package br.ufpr.dac.contasService.messaging.saga.producer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.contasService.entity.Conta;
import br.ufpr.dac.contasService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GetIdGerenteComMaisContasHandler {

  private RabbitTemplate template;
  private ContaRepository repo;

  public void HandleGetIdGerenteComMaisContas(SagaMessageWrapper<Long> message) {

    // organiza contas em mapa com seu gerente
    var contasPorGerente = new HashMap<Long, List<Conta>>();
    repo.findAll().forEach(conta -> {
      contasPorGerente.computeIfAbsent(conta.getGerente(), key -> new ArrayList<Conta>()).add(conta);
    });
    // transforma lista no tamanho dela
    var numeroDeContasPorGerente = new HashMap<Long, Integer>();
    contasPorGerente.forEach((gerenteId, contas) -> {
      numeroDeContasPorGerente.put(gerenteId, contas.size());
    });
    // coleta gerentes com a maior conta
    Optional<Integer> maiorValorOpt = numeroDeContasPorGerente.entrySet()
        .stream()
        .max(Comparator.comparingInt(Map.Entry::getValue))
        .map(Map.Entry::getValue);
    int maiorValor = maiorValorOpt.get();
    List<Long> gerentesComMaisContas = numeroDeContasPorGerente.entrySet()
        .stream()
        .filter(entry -> entry.getValue() == maiorValor)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    Long gerenteEscolhido = gerentesComMaisContas.get(new Random().nextInt(gerentesComMaisContas.size()));

    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            MessageOperations.RESULT,
            List.of(gerenteEscolhido),
            message.getCorrelationId()));

  }

  public void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_QUEUE,
        message);
  }

};
