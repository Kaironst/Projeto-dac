package br.ufpr.dac.contasService.messaging.saga;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.contasService.entity.Conta;
import br.ufpr.dac.contasService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.AutocadastroDto;
import br.ufpr.dac.shared.dto.ContasDto;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CriarContaAutocadastroHandler {

  private final RabbitTemplate template;
  private final ContaRepository repo;

  @Transactional
  public void handleCriarConta(SagaMessageWrapper<AutocadastroDto.Solicitacao> message) {
    try {
      var solicitacao = message.getData().getFirst();
      var cliente = solicitacao.getCliente();
      var gerente = solicitacao.getGerente();

      if (cliente == null || cliente.getId() == null || gerente == null || gerente.getId() == null) {
        throw new IllegalArgumentException("Solicitacao aprovada sem cliente ou gerente.");
      }

      if (!repo.findAllByCliente(cliente.getId()).isEmpty()) {
        throw new IllegalStateException("Cliente ja possui conta.");
      }

      var conta = Conta.builder()
          .numero(gerarNumeroConta())
          .cliente(cliente.getId())
          .gerente(gerente.getId())
          .saldo(0.0)
          .limite(calcularLimite(cliente.getSalario()))
          .dataCriacao(LocalDate.now())
          .build();

      var contaCriada = repo.save(conta);
      var result = AutocadastroDto.ContaCriada.builder()
          .solicitacaoId(solicitacao.getId())
          .conta(toDto(contaCriada))
          .build();

      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.ContaCriada>(
          SagaOperations.Autocadastro.CRIAR_CONTA_RESULT,
          List.of(result),
          message.getCorrelationId()));
    } catch (Exception e) {
      e.printStackTrace();
      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.ContaCriada>(
          SagaOperations.Autocadastro.CRIAR_CONTA_ERROR,
          List.of(),
          message.getCorrelationId()));
    }
  }

  private String gerarNumeroConta() {
    for (int tentativas = 0; tentativas < 10000; tentativas++) {
      var numero = Integer.toString(ThreadLocalRandom.current().nextInt(1000, 10000));
      if (!repo.existsByNumero(numero)) {
        return numero;
      }
    }

    throw new IllegalStateException("Nao foi possivel gerar numero de conta unico.");
  }

  private double calcularLimite(Double salario) {
    if (salario == null || salario < 2000.0) {
      return 0.0;
    }

    return salario / 2.0;
  }

  private ContasDto.Conta toDto(Conta conta) {
    return ContasDto.Conta.builder()
        .id(conta.getId())
        .numero(conta.getNumero())
        .saldo(conta.getSaldo())
        .limite(conta.getLimite())
        .cliente(UsersDto.Cliente.builder().id(conta.getCliente()).build())
        .gerente(GerentesDto.Gerente.builder().id(conta.getGerente()).build())
        .dataCriacao(conta.getDataCriacao())
        .build();
  }

  private void enviarMensagem(SagaMessageWrapper<AutocadastroDto.ContaCriada> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

}
