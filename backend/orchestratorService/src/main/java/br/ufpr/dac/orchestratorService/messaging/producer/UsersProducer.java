package br.ufpr.dac.orchestratorService.messaging.producer;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.keys.RabbitmqConsts;

@Service
public class UsersProducer {

  @Autowired
  private RabbitTemplate template;

  public UsersDto.Message enviarMenssagem(String operacao, List<UsersDto.Cliente> clientes) {
    var response = (UsersDto.Message) template.convertSendAndReceiveAsType(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.USERS_KEY,
        new UsersDto.Message(operacao, clientes),
        new ParameterizedTypeReference<UsersDto.Message>() {
        });

    if (response == null)
      System.out.println("error on enviarMenssagem from usersService");
    return response == null ? new UsersDto.Message("ERROR", null) : response;

  }

  public UsersDto.Message createCliente(UsersDto.Cliente cliente) {
    System.out.println(cliente);
    return enviarMenssagem("CREATE", List.of(cliente));
  }

  public UsersDto.Message readCliente(long id) {
    var clienteId = UsersDto.Cliente.builder().id(id).build();
    return enviarMenssagem("READ", List.of(clienteId));
  }

  public UsersDto.Message readAllClientes() {
    return enviarMenssagem("READ_ALL", null);
  }

  public UsersDto.Message updateCliente(UsersDto.Cliente cliente) {
    return enviarMenssagem("UPDATE", List.of(cliente));
  }

  public UsersDto.Message deleteCliente(Long id) {
    var clienteId = UsersDto.Cliente.builder().id(id).build();
    return enviarMenssagem("DELETE", List.of(clienteId));
  }

};
