/*TODO: decidir se é melhor integrar item histórico aqui ou 
 * se deixa o histórico navegando pelo dto*/
package br.ufpr.dac.contasService.messaging.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.contasService.entity.Conta;
import br.ufpr.dac.contasService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.ContasDto;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.dto.ItemHistoricoDto;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MessageConsumer {

  private final ContaRepository repo;

  @RabbitListener(queues = RabbitmqConsts.CONTAS_QUEUE)
  @Transactional
  public MessageWrapper<ContasDto.Conta> recieve(MessageWrapper<ContasDto.Conta> message) {
    try {
      switch (message.getOperation()) {
        case MessageOperations.CREATE -> {
          return handleCreate(message.getData());
        }
        case MessageOperations.READ -> {
          return handleRead(message.getData());
        }
        case MessageOperations.READ_ALL -> {
          return handleReadAll();
        }
        case MessageOperations.UPDATE -> {
          return handleUpdate(message.getData());
        }
        case MessageOperations.DELETE -> {
          return handleDelete(message.getData());
        }
        case MessageOperations.DEPOSITO -> {
          return handleDeposito(message.getData());
        }
        case MessageOperations.SAQUE -> {
          return handleSaque(message.getData());
        }
        case MessageOperations.TRANSFERENCIA -> {
          return handleTransferencia(message.getData());
        }
        default -> {
          throw new UnsupportedOperationException();
        }
      }
    } catch (Exception e) {
      System.out.println("error on message consumer listener");
      e.printStackTrace();
      return new MessageWrapper<ContasDto.Conta>(MessageOperations.ERROR_GENERIC, null);
    }

  }

  public static List<ContasDto.Conta> contasToDto(List<Conta> contas) {
    final var contasDto = new ArrayList<ContasDto.Conta>();
    contas.forEach(conta -> {
      var extrato = new ArrayList<ItemHistoricoDto.ItemHistorico>();
      if (conta.getHistoricoOrigem() != null) {
          conta.getHistoricoOrigem().forEach(h -> {
              extrato.add(ItemHistoricoDto.ItemHistorico.builder()
                  .id(h.getId())
                  .dataHora(h.getDataHora())
                  .tipo(h.getTipo())
                  .valorMovimentacao(h.getValorMovimentacao())
                  .build());
          });
      }
      if (conta.getHistoricoDestino() != null) {
          conta.getHistoricoDestino().forEach(h -> {
              // nao duplica transferencias onde origem e destino são a mesma conta
              if (h.getContaOrigem() == null || !h.getContaOrigem().getId().equals(conta.getId())) {
                  extrato.add(ItemHistoricoDto.ItemHistorico.builder()
                      .id(h.getId())
                      .dataHora(h.getDataHora())
                      .tipo(h.getTipo())
                      .valorMovimentacao(h.getValorMovimentacao())
                      .build());
              }
          });
      }
      extrato.sort(Comparator.comparing(ItemHistoricoDto.ItemHistorico::getDataHora).reversed());

      var contaDto = ContasDto.Conta.builder()
          .id(conta.getId())
          .numero(conta.getNumero())
          .saldo(conta.getSaldo())
          .limite(conta.getLimite())
          .cliente(UsersDto.Cliente.builder().id(conta.getCliente()).build())
          .gerente(GerentesDto.Gerente.builder().id(conta.getGerente()).build())
          .dataCriacao(conta.getDataCriacao())
          .extrato(extrato)
          .build();

      contasDto.add(contaDto);
    });
    return contasDto;
  }

  public static List<Conta> dtoToContas(List<ContasDto.Conta> contasDto) {
    final var contas = new ArrayList<Conta>();
    contasDto.forEach(contaDto -> {
      var conta = Conta.builder()
          .id(contaDto.getId())
          .numero(contaDto.getNumero())
          .saldo(contaDto.getSaldo())
          .limite(contaDto.getLimite())
          .cliente(contaDto.getCliente().getId())
          .gerente(contaDto.getGerente().getId())
          .dataCriacao(contaDto.getDataCriacao())
          .build();

      contas.add(conta);
    });
    return contas;
  }

  @Transactional
  private MessageWrapper<ContasDto.Conta> handleCreate(List<ContasDto.Conta> contas) {
    List<Conta> queryResult = repo.saveAll(dtoToContas(contas));
    return new MessageWrapper<ContasDto.Conta>(MessageOperations.RESULT, contasToDto(queryResult));
  }

  @Transactional(readOnly = true)
  private MessageWrapper<ContasDto.Conta> handleRead(List<ContasDto.Conta> contas) {
    final var idList = new ArrayList<Long>();
    contas.forEach(conta -> idList.add(conta.getId()));
    List<Conta> queryResult = repo.findAllById(idList);
    return new MessageWrapper<ContasDto.Conta>(MessageOperations.RESULT, contasToDto(queryResult));
  }

  @Transactional(readOnly = true)
  private MessageWrapper<ContasDto.Conta> handleReadAll() {
    List<Conta> queryResult = repo.findAll();
    return new MessageWrapper<ContasDto.Conta>(MessageOperations.RESULT, contasToDto(queryResult));
  }

  @Transactional
  private MessageWrapper<ContasDto.Conta> handleUpdate(List<ContasDto.Conta> contas) {
    var ContasAtualizadas = new ArrayList<Conta>();

    dtoToContas(contas).forEach(conta -> {
      Conta contaAtual = repo.findById(conta.getId()).orElseThrow();

      contaAtual.setNumero(conta.getNumero());
      contaAtual.setSaldo(conta.getSaldo());
      contaAtual.setLimite(conta.getLimite());
      contaAtual.setCliente(conta.getCliente());
      contaAtual.setGerente(conta.getGerente());

      ContasAtualizadas.add(repo.save(contaAtual));
    });
    return new MessageWrapper<ContasDto.Conta>(MessageOperations.RESULT, contasToDto(ContasAtualizadas));
  }

  @Transactional
  private MessageWrapper<ContasDto.Conta> handleDelete(List<ContasDto.Conta> contas) {
    final var idList = new ArrayList<Long>();
    contas.forEach(conta -> idList.add(conta.getId()));
    repo.deleteAllById(idList);
    return new MessageWrapper<ContasDto.Conta>(MessageOperations.RESULT, null);
  }

  @Transactional
  private MessageWrapper<ContasDto.Conta> handleDeposito(List<ContasDto.Conta> contas) {
    var contasAtualizadas = new ArrayList<Conta>();
    contas.forEach(contaDto -> {
      Conta contaAtual = contaDto.getId() != null && contaDto.getId() > 0 ? repo.findById(contaDto.getId()).orElseThrow() : repo.findByNumero(contaDto.getNumero()).orElseThrow();
      Double valorDeposito = contaDto.getSaldo();
      contaAtual.setSaldo(contaAtual.getSaldo() + valorDeposito);
      
      br.ufpr.dac.contasService.entity.ItemHistorico h = br.ufpr.dac.contasService.entity.ItemHistorico.builder()
          .contaDestino(contaAtual)
          .dataHora(LocalDateTime.now())
          .tipo(ItemHistoricoDto.TipoTransacao.DEPOSITO)
          .valorMovimentacao(valorDeposito)
          .build();

      if (contaAtual.getHistoricoDestino() == null) contaAtual.setHistoricoDestino(new ArrayList<>());
      contaAtual.getHistoricoDestino().add(h);

      contasAtualizadas.add(repo.save(contaAtual));
    });
    return new MessageWrapper<ContasDto.Conta>(MessageOperations.RESULT, contasToDto(contasAtualizadas));
  }

  @Transactional
  private MessageWrapper<ContasDto.Conta> handleSaque(List<ContasDto.Conta> contas) {
    var contasAtualizadas = new ArrayList<Conta>();
    contas.forEach(contaDto -> {
      Conta contaAtual = contaDto.getId() != null && contaDto.getId() > 0 ? repo.findById(contaDto.getId()).orElseThrow() : repo.findByNumero(contaDto.getNumero()).orElseThrow();
      Double valorSaque = contaDto.getSaldo();
      if (contaAtual.getSaldo() + contaAtual.getLimite() < valorSaque) {
         throw new RuntimeException("Saldo insuficiente");
      }
      contaAtual.setSaldo(contaAtual.getSaldo() - valorSaque);

      br.ufpr.dac.contasService.entity.ItemHistorico h = br.ufpr.dac.contasService.entity.ItemHistorico.builder()
          .contaOrigem(contaAtual)
          .dataHora(LocalDateTime.now())
          .tipo(ItemHistoricoDto.TipoTransacao.SAQUE)
          .valorMovimentacao(valorSaque)
          .build();

      if (contaAtual.getHistoricoOrigem() == null) contaAtual.setHistoricoOrigem(new ArrayList<>());
      contaAtual.getHistoricoOrigem().add(h);

      contasAtualizadas.add(repo.save(contaAtual));
    });
    return new MessageWrapper<ContasDto.Conta>(MessageOperations.RESULT, contasToDto(contasAtualizadas));
  }

  @Transactional
  private MessageWrapper<ContasDto.Conta> handleTransferencia(List<ContasDto.Conta> contas) {
      if (contas.size() < 2) throw new IllegalArgumentException("Need 2 accounts for transfer");
      Conta origem = contas.get(0).getId() != null && contas.get(0).getId() > 0 ? repo.findById(contas.get(0).getId()).orElseThrow() : repo.findByNumero(contas.get(0).getNumero()).orElseThrow();
      Conta destino = contas.get(1).getId() != null && contas.get(1).getId() > 0 ? repo.findById(contas.get(1).getId()).orElseThrow() : repo.findByNumero(contas.get(1).getNumero()).orElseThrow();
      
      Double valor = contas.get(0).getSaldo();
      if (origem.getSaldo() + origem.getLimite() < valor) {
         throw new RuntimeException("Saldo insuficiente");
      }
      
      origem.setSaldo(origem.getSaldo() - valor);
      destino.setSaldo(destino.getSaldo() + valor);
      
      br.ufpr.dac.contasService.entity.ItemHistorico h = br.ufpr.dac.contasService.entity.ItemHistorico.builder()
          .contaOrigem(origem)
          .contaDestino(destino)
          .dataHora(LocalDateTime.now())
          .tipo(ItemHistoricoDto.TipoTransacao.TRANSACAO)
          .valorMovimentacao(valor)
          .build();

      if (origem.getHistoricoOrigem() == null) origem.setHistoricoOrigem(new ArrayList<>());
      origem.getHistoricoOrigem().add(h);

      if (destino.getHistoricoDestino() == null) destino.setHistoricoDestino(new ArrayList<>());
      destino.getHistoricoDestino().add(h);

      repo.save(destino);
      var origemSalva = repo.save(origem);
      
      return new MessageWrapper<ContasDto.Conta>(MessageOperations.RESULT, contasToDto(List.of(origemSalva)));
  }

}
