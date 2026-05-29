package br.ufpr.dac.contasService.messaging.saga;

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
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.contasService.entity.Conta;
import br.ufpr.dac.contasService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GetIdGerenteComMaisContasHandler {

  private RabbitTemplate template;
  private ContaRepository repo;

  @Transactional(readOnly = true)
  public void HandleGetIdGerenteComMaisContas(SagaMessageWrapper<Long> message) {

    Long gerenteEscolhido = 0l;
    boolean sucesso = true;
    try {
      // organiza contas em mapa com seu gerente
      var contasPorGerente = new HashMap<Long, List<Conta>>();
      repo.findAll().forEach(conta -> {
        if (conta.getGerente() != null) {
            contasPorGerente.computeIfAbsent(conta.getGerente(), key -> new ArrayList<Conta>()).add(conta);
        }
      });

      if (contasPorGerente.isEmpty()) {
          gerenteEscolhido = 0L;
      } else {
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
          
          int maiorValor = maiorValorOpt.orElse(0);

          if (maiorValor <= 1 && contasPorGerente.size() <= 1) {
              gerenteEscolhido = 0L;
          } else {
              List<Long> gerentesComMaisContas = numeroDeContasPorGerente.entrySet()
                  .stream()
                  .filter(entry -> entry.getValue() == maiorValor)
                  .map(Map.Entry::getKey)
                  .collect(Collectors.toList());

              if (gerentesComMaisContas.size() == 1) {
                  gerenteEscolhido = gerentesComMaisContas.get(0);
              } else {
                  // Desempate: menor saldo positivo
                  gerenteEscolhido = gerentesComMaisContas.stream().min(Comparator.comparingDouble(gId -> {
                      return contasPorGerente.get(gId).stream()
                          .filter(c -> c.getSaldo() != null && c.getSaldo() >= 0)
                          .mapToDouble(Conta::getSaldo)
                          .sum();
                  })).orElse(0L);
              }
          }
      }
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }

    if (gerenteEscolhido == null) {
      sucesso = false;
      gerenteEscolhido = 0l;
    }

    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.InsertGerente.GET_COM_MAIS_CONTAS_RESULT
                : SagaOperations.InsertGerente.GET_COM_MAIS_CONTAS_ERROR,
            List.of(gerenteEscolhido),
            message.getCorrelationId()));

  }

  public void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
    System.out.println("message" + message + "sent to " + RabbitmqConsts.ORCHESTRATOR_SAGA_QUEUE);
  }

};
