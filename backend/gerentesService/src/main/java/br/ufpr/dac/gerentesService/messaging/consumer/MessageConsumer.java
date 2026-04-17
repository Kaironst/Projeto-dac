package br.ufpr.dac.gerentesService.messaging.consumer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.gerentesService.entity.Gerente;
import br.ufpr.dac.gerentesService.repository.GerenteRepository;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MessageConsumer {

  private final GerenteRepository repo;

  @RabbitListener(queues = RabbitmqConsts.GERENTES_QUEUE)
  public GerentesDto.Message recieve(GerentesDto.Message message) {
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
      return new GerentesDto.Message("ERROR", null);
    }

  }

  public static List<GerentesDto.Gerente> gerentesToDto(List<Gerente> gerentes) {
    final var gerentesDto = new ArrayList<GerentesDto.Gerente>();
    gerentes.forEach(gerente -> {
      var gerenteDto = GerentesDto.Gerente.builder()
          .id(gerente.getId())
          .nome(gerente.getNome())
          .email(gerente.getEmail())
          .telefone(gerente.getTelefone())
          .cpf(gerente.getCpf())
          .build();
      gerentesDto.add(gerenteDto);
    });
    return gerentesDto;
  }

  public static List<Gerente> dtoToGerentes(List<GerentesDto.Gerente> gerentesDto) {
    final var gerentes = new ArrayList<Gerente>();
    gerentesDto.forEach(gerenteDto -> {
      var gerente = Gerente.builder()
          .id(gerenteDto.getId())
          .nome(gerenteDto.getNome())
          .email(gerenteDto.getEmail())
          .telefone(gerenteDto.getTelefone())
          .cpf(gerenteDto.getCpf())
          .build();
      gerentes.add(gerente);
    });
    return gerentes;
  }

  @Transactional
  private GerentesDto.Message handleCreate(List<GerentesDto.Gerente> gerentes) {
    List<Gerente> queryResult = repo.saveAll(dtoToGerentes(gerentes));
    return new GerentesDto.Message(MessageOperations.RESULT, gerentesToDto(queryResult));
  }

  @Transactional(readOnly = true)
  private GerentesDto.Message handleRead(List<GerentesDto.Gerente> gerentes) {
    final var idList = new ArrayList<Long>();
    gerentes.forEach(gerente -> idList.add(gerente.getId()));
    List<Gerente> queryResult = repo.findAllById(idList);
    return new GerentesDto.Message(MessageOperations.RESULT, gerentesToDto(queryResult));
  }

  @Transactional(readOnly = true)
  private GerentesDto.Message handleReadAll() {
    List<Gerente> queryResult = repo.findAll();
    return new GerentesDto.Message(MessageOperations.RESULT, gerentesToDto(queryResult));
  }

  @Transactional
  private GerentesDto.Message handleUpdate(List<GerentesDto.Gerente> gerentes) {
    var gerentesAtualizados = new ArrayList<Gerente>();

    gerentes.forEach(gerente -> {
      Gerente gerenteAtual = repo.findById(gerente.getId()).orElseThrow();

      gerenteAtual.setNome(gerente.getNome());
      gerenteAtual.setEmail(gerente.getEmail());
      gerenteAtual.setTelefone(gerente.getTelefone());
      gerenteAtual.setCpf(gerente.getCpf());

      gerentesAtualizados.add(repo.save(gerenteAtual));
    });
    return new GerentesDto.Message(MessageOperations.RESULT, gerentesToDto(gerentesAtualizados));
  }

  @Transactional
  private GerentesDto.Message handleDelete(List<GerentesDto.Gerente> gerentes) {
    final var idList = new ArrayList<Long>();
    gerentes.forEach(gerente -> idList.add(gerente.getId()));
    repo.deleteAllById(idList);
    return new GerentesDto.Message(MessageOperations.RESULT, null);
  }

}
