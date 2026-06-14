package br.ufpr.dac.contasService.messaging.saga.autocadastro;

import java.time.LocalDate;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.ufpr.dac.contasService.entity.Conta;
import br.ufpr.dac.contasService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AutocadastroCriarConta {

  private final RabbitTemplate template;
  private final ContaRepository repo;

  public void handleCriarConta(SagaMessageWrapper<Double> message) {

    boolean sucesso = true;
    Long contaId = null;
    try {
      // pega os valores enviados da lista
      Long idCliente = message.getData().get(0).longValue();
      Long idGerente = message.getData().get(1).longValue();
      Double salarioCliente = message.getData().get(3);

      Conta novaConta = Conta.builder()
          .cliente(idCliente)
          .gerente(idGerente)
          .limite(salarioCliente / 2)
          .saldo(0d)
          .dataCriacao(LocalDate.now())
          .build();
      repo.save(novaConta);

      contaId = novaConta.getId();

    } catch (Exception e) {
      sucesso = false;
      e.printStackTrace();
    }

    enviarMenssagem(new SagaMessageWrapper<Long>(
        sucesso ? SagaOperations.Autocadastro.CRIAR_CONTA_RESULT
            : SagaOperations.Autocadastro.CRIAR_CONTA_ERROR,
        sucesso ? List.of(contaId)
            : List.of(),
        message.getCorrelationId()));
  }

  private void enviarMenssagem(SagaMessageWrapper<Long> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);

  }

}
