package br.ufpr.dac.orchestratorService.messaging.producer;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufpr.dac.orchestratorService.config.RabbitMQConfig;
import br.ufpr.dac.orchestratorService.messaging.dto.UsersDto;

@Service
public class UsersProducer {

  @Autowired
  private RabbitTemplate template;

  public void enviarMenssagem(String operacao, UsersDto.Cliente cliente) {

    var menssagem = UsersDto.Message.builder()
        .data(List.of(cliente))
        .operation(operacao)
        .build();

    template.convertAndSend(
        RabbitMQConfig.APP_EXCHANGE,
        RabbitMQConfig.USERS_KEY,
        menssagem);
  }

  public void createCliente(UsersDto.Cliente cliente) {
    enviarMenssagem("CREATE", cliente);
  }

  public void readCliente(long id) {
    var clienteId = UsersDto.Cliente.builder().id(id).build();
    enviarMenssagem("READ", clienteId);
  }

  public void updateCliente(UsersDto.Cliente cliente) {
    enviarMenssagem("UPDATE", cliente);
  }

  public void deleteCliente(long id) {
    var clienteId = UsersDto.Cliente.builder().id(id).build();
    enviarMenssagem("DELETE", clienteId);
  }

};
