package br.ufpr.dac.contasService.messaging.saga.autocadastro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    Long gerenteARemoverId = null;
    List<Long> gerentesAtivos = new ArrayList<>();

    if (message.getData() != null && !message.getData().isEmpty()) {
      gerenteARemoverId = message.getData().get(0);
      if (message.getData().size() > 1) {
        gerentesAtivos.addAll(message.getData().subList(1, message.getData().size()));
      }
    }

    Long gerenteEscolhido = 0L;
    boolean sucesso = true;

    try {
      if (gerentesAtivos.isEmpty()) {
        sucesso = false;
      } else {
        // Remove the one being removed from consideration
        if (gerenteARemoverId != null) {
          gerentesAtivos.remove(gerenteARemoverId);
        }

        if (gerentesAtivos.isEmpty()) {
          // Cannot move accounts if no other gerentes exist
          sucesso = false;
        } else {
          var numeroDeContasPorGerente = new HashMap<Long, Integer>();
          for (Long gId : gerentesAtivos) {
            numeroDeContasPorGerente.put(gId, 0); // initialize all known gerentes with 0
          }

          // Count accounts
          repo.findAll().forEach(conta -> {
            if (numeroDeContasPorGerente.containsKey(conta.getGerente())) {
              numeroDeContasPorGerente.put(conta.getGerente(), numeroDeContasPorGerente.get(conta.getGerente()) + 1);
            }
          });

          Optional<Integer> menorValorOpt = numeroDeContasPorGerente.values().stream().min(Integer::compare);

          if (menorValorOpt.isPresent()) {
            int menorValor = menorValorOpt.get();
            List<Long> gerentesComMenosContas = numeroDeContasPorGerente.entrySet().stream()
                .filter(entry -> entry.getValue() == menorValor)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

            // If tie, pick any (random)
            gerenteEscolhido = gerentesComMenosContas.get(new Random().nextInt(gerentesComMenosContas.size()));
          } else {
            sucesso = false;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      sucesso = false;
    }

    this.enviarMenssagem(
        new SagaMessageWrapper<Long>(
            sucesso ? SagaOperations.Autocadastro.GET_GERENTE_MENOS_CONTAS_RESULT
                : SagaOperations.Autocadastro.GET_GERENTE_MENOS_CONTAS_ERROR,
            List.of(gerenteEscolhido),
            message.getCorrelationId()));

  }

  public void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

}
