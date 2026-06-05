package br.ufpr.dac.cqrsService.messaging.consumer;

import java.util.List;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
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
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class RequestConsumer {

  private final ClienteDtoModel clienteDtoModel;
  private final EnderecoDtoModel enderecoDtoModel;
  private final GerenteDtoModel gerenteDtoModel;
  private final ContaDtoModel contaDtoModel;
  private final ItemHistoricoDtoModel itemHistoricoDtoModel;
  private final ObjectMapper objectMapper;

  @RabbitListener(queues = RabbitmqConsts.CQRS_REQUEST_QUEUE)
  private MessageWrapper<?> recieve(MessageWrapper<?> message) throws AmqpRejectAndDontRequeueException {

    if (message.getData().isEmpty() || message.getData() == null)
      throw new AmqpRejectAndDontRequeueException("lista do corpo vazia ou nula");

    return switch (message.getDataType()) {
      case MessageWrapper.DataTypes.cliente ->
        handleClienteRequest(message);
      case MessageWrapper.DataTypes.endereco ->
        handleEnderecoRequest(message);
      case MessageWrapper.DataTypes.gerente ->
        handleGerenteRequest(message);
      case MessageWrapper.DataTypes.conta ->
        handleContaRequest(message);
      case MessageWrapper.DataTypes.itemHistorico ->
        handleItemHistoricoRequest(message);
      case null, default ->
        throw new AmqpRejectAndDontRequeueException("tipo de menssagem não suportado");
    };

  }

  private MessageWrapper<UsersDto.Cliente> handleClienteRequest(MessageWrapper<?> message) {

    var target = objectMapper.convertValue(message.getData().getFirst(), UsersDto.Cliente.class);
    List<UsersDto.Cliente> encontrado = null;

    if (message.getOperation().equals(MessageOperations.READ))
      encontrado = List.of(clienteDtoModel.handleRead(target.getId()));

    else if (message.getOperation().equals(MessageOperations.READ_ALL))
      encontrado = clienteDtoModel.handleReadAll();

    if (encontrado == null)
      throw new AmqpRejectAndDontRequeueException("operação de leitura não implementada");

    return new MessageWrapper<UsersDto.Cliente>(MessageOperations.RESULT, encontrado);

  }

  private MessageWrapper<UsersDto.Endereco> handleEnderecoRequest(MessageWrapper<?> message) {

    var target = objectMapper.convertValue(message.getData().getFirst(), UsersDto.Endereco.class);
    List<UsersDto.Endereco> encontrado = null;

    if (message.getOperation().equals(MessageOperations.READ))
      encontrado = List.of(enderecoDtoModel.handleRead(target.getId()));

    else if (message.getOperation().equals(MessageOperations.READ_ALL))
      encontrado = enderecoDtoModel.handleReadAll();

    if (encontrado == null)
      throw new AmqpRejectAndDontRequeueException("operação de leitura não implementada");

    return new MessageWrapper<UsersDto.Endereco>(MessageOperations.RESULT, encontrado);

  }

  private MessageWrapper<GerentesDto.Gerente> handleGerenteRequest(MessageWrapper<?> message) {
    var target = objectMapper.convertValue(message.getData().getFirst(), GerentesDto.Gerente.class);
    List<GerentesDto.Gerente> encontrado = null;

    if (message.getOperation().equals(MessageOperations.READ))
      encontrado = List.of(gerenteDtoModel.handleRead(target.getId()));

    else if (message.getOperation().equals(MessageOperations.READ_ALL))
      encontrado = gerenteDtoModel.handleReadAll();

    if (encontrado == null)
      throw new AmqpRejectAndDontRequeueException("operação de leitura não implementada");

    return new MessageWrapper<GerentesDto.Gerente>(MessageOperations.RESULT, encontrado);

  }

  private MessageWrapper<ContasDto.Conta> handleContaRequest(MessageWrapper<?> message) {
    var target = objectMapper.convertValue(message.getData().getFirst(), ContasDto.Conta.class);
    List<ContasDto.Conta> encontrado = null;

    if (message.getOperation().equals(MessageOperations.READ))
      encontrado = List.of(contaDtoModel.handleRead(target.getId()));

    else if (message.getOperation().equals(MessageOperations.READ_ALL))
      encontrado = contaDtoModel.handleReadAll();

    if (encontrado == null)
      throw new AmqpRejectAndDontRequeueException("operação de leitura não implementada");

    return new MessageWrapper<ContasDto.Conta>(MessageOperations.RESULT, encontrado);

  }

  private MessageWrapper<ItemHistoricoDto.ItemHistorico> handleItemHistoricoRequest(
      MessageWrapper<?> message) {
    var target = objectMapper.convertValue(message.getData().getFirst(), ItemHistoricoDto.ItemHistorico.class);
    List<ItemHistoricoDto.ItemHistorico> encontrado = null;

    if (message.getOperation().equals(MessageOperations.READ))
      encontrado = List.of(itemHistoricoDtoModel.handleRead(target.getId()));

    else if (message.getOperation().equals(MessageOperations.READ_ALL))
      encontrado = itemHistoricoDtoModel.handleReadAll();

    if (encontrado == null)
      throw new AmqpRejectAndDontRequeueException("operação de leitura não implementada");

    return new MessageWrapper<ItemHistoricoDto.ItemHistorico>(MessageOperations.RESULT, encontrado);

  }

}
