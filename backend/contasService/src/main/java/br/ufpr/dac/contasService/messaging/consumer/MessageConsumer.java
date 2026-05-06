/*TODO: decidir se é melhor integrar item histórico aqui ou 
 * se deixa o histórico navegando pelo dto*/
package br.ufpr.dac.contasService.messaging.consumer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.contasService.entity.Conta;
import br.ufpr.dac.contasService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.ContasDto;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MessageConsumer {

  private final ContaRepository repo;

  @RabbitListener(queues = RabbitmqConsts.CONTAS_QUEUE)
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
      var contaDto = ContasDto.Conta.builder()
          .id(conta.getId())
          .numero(conta.getNumero())
          .saldo(conta.getSaldo())
          .limite(conta.getLimite())
          .cliente(UsersDto.Cliente.builder().id(conta.getCliente()).build())
          .gerente(GerentesDto.Gerente.builder().id(conta.getGerente()).build())
          .dataCriacao(conta.getDataCriacao())
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

}
