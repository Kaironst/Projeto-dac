package br.ufpr.dac.cqrsService.messaging.consumer;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.cqrsService.model.ClienteDtoModel;
import br.ufpr.dac.cqrsService.model.ContaDtoModel;
import br.ufpr.dac.cqrsService.model.EnderecoDtoModel;
import br.ufpr.dac.cqrsService.model.GerenteDtoModel;
import br.ufpr.dac.cqrsService.model.ItemHistoricoDtoModel;
import br.ufpr.dac.shared.dto.ContasDto;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.ItemHistoricoDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class RequestConsumer {

  private final ClienteDtoModel clienteDtoModel;
  private final EnderecoDtoModel enderecoDtoModel;
  private final GerenteDtoModel gerenteDtoModel;
  private final ContaDtoModel contaDtoModel;
  private final ItemHistoricoDtoModel itemHistoricoDtoModel;

  @SuppressWarnings({ "unchecked", "unused" })
  @RabbitListener(queues = RabbitmqConsts.CQRS_REQUEST_QUEUE)
  private MessageWrapper<?> recieve(MessageWrapper<?> message) throws UnsupportedOperationException {

    var list = message.getData();
    if (list.isEmpty() || list == null)
      throw new UnsupportedOperationException("lista do corpo vazia ou nula");

    return switch (list.getFirst()) {
      case UsersDto.Cliente m ->
        handleClienteRequest((MessageWrapper<UsersDto.Cliente>) message);
      case UsersDto.Endereco m ->
        handleEnderecoRequest((MessageWrapper<UsersDto.Endereco>) message);
      case GerentesDto.Gerente m ->
        handleGerenteRequest((MessageWrapper<GerentesDto.Gerente>) message);
      case ContasDto.Conta m ->
        handleContaRequest((MessageWrapper<ContasDto.Conta>) message);
      case ItemHistoricoDto.ItemHistorico m ->
        handleItemHistoricoRequest((MessageWrapper<ItemHistoricoDto.ItemHistorico>) message);
      case null, default ->
        throw new UnsupportedOperationException("tipo de menssagem não suportado");
    };

  }

  private MessageWrapper<UsersDto.Cliente> handleClienteRequest(MessageWrapper<UsersDto.Cliente> message) {
    var target = message.getData().getFirst();
    List<UsersDto.Cliente> encontrado = null;
    if (message.getOperation() == MessageOperations.READ)
      encontrado = List.of(clienteDtoModel.handleRead(target.getId()));
    else if (message.getOperation() == MessageOperations.READ_ALL)
      encontrado = clienteDtoModel.handleReadAll();
    if (encontrado == null)
      throw new UnsupportedOperationException("operação de leitura não implementada");
    return new MessageWrapper<UsersDto.Cliente>(MessageOperations.RESULT, encontrado);
  }

  private MessageWrapper<UsersDto.Endereco> handleEnderecoRequest(MessageWrapper<UsersDto.Endereco> message) {
    var target = message.getData().getFirst();
    List<UsersDto.Endereco> encontrado = null;
    if (message.getOperation() == MessageOperations.READ)
      encontrado = List.of(enderecoDtoModel.handleRead(target.getId()));
    else if (message.getOperation() == MessageOperations.READ_ALL)
      encontrado = enderecoDtoModel.handleReadAll();
    if (encontrado == null)
      throw new UnsupportedOperationException("operação de leitura não implementada");
    return new MessageWrapper<UsersDto.Endereco>(MessageOperations.RESULT, encontrado);
  }

  private MessageWrapper<GerentesDto.Gerente> handleGerenteRequest(MessageWrapper<GerentesDto.Gerente> message) {
    var target = message.getData().getFirst();
    List<GerentesDto.Gerente> encontrado = null;
    if (message.getOperation() == MessageOperations.READ)
      encontrado = List.of(gerenteDtoModel.handleRead(target.getId()));
    else if (message.getOperation() == MessageOperations.READ_ALL)
      encontrado = gerenteDtoModel.handleReadAll();
    if (encontrado == null)
      throw new UnsupportedOperationException("operação de leitura não implementada");
    return new MessageWrapper<GerentesDto.Gerente>(MessageOperations.RESULT, encontrado);
  }

  private MessageWrapper<ContasDto.Conta> handleContaRequest(MessageWrapper<ContasDto.Conta> message) {
    var target = message.getData().getFirst();
    List<ContasDto.Conta> encontrado = null;
    if (message.getOperation() == MessageOperations.READ)
      encontrado = List.of(contaDtoModel.handleRead(target.getId()));
    else if (message.getOperation() == MessageOperations.READ_ALL)
      encontrado = contaDtoModel.handleReadAll();
    if (encontrado == null)
      throw new UnsupportedOperationException("operação de leitura não implementada");
    return new MessageWrapper<ContasDto.Conta>(MessageOperations.RESULT, encontrado);
  }

  private MessageWrapper<ItemHistoricoDto.ItemHistorico> handleItemHistoricoRequest(
      MessageWrapper<ItemHistoricoDto.ItemHistorico> message) {
    var target = message.getData().getFirst();
    List<ItemHistoricoDto.ItemHistorico> encontrado = null;
    if (message.getOperation() == MessageOperations.READ)
      encontrado = List.of(itemHistoricoDtoModel.handleRead(target.getId()));
    else if (message.getOperation() == MessageOperations.READ_ALL)
      encontrado = itemHistoricoDtoModel.handleReadAll();
    if (encontrado == null)
      throw new UnsupportedOperationException("operação de leitura não implementada");
    return new MessageWrapper<ItemHistoricoDto.ItemHistorico>(MessageOperations.RESULT, encontrado);
  }

}
