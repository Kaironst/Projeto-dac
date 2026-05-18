package br.ufpr.dac.authService.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.authService.service.AuthService;
import br.ufpr.dac.shared.dto.AuthDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MessageConsumer {

  private final AuthService authService;

  @RabbitListener(queues = RabbitmqConsts.AUTH_QUEUE)
  public MessageWrapper<AuthDto.LoginResponse> recieve(MessageWrapper<AuthDto.LoginRequest> message) {
    try {
      switch (message.getOperation()) {
        case MessageOperations.LOGIN -> {
          return handleLogin(message);
        }
        default -> throw new UnsupportedOperationException();
      }
    } catch (Exception e) {
      System.out.println("error on auth message consumer listener");
      e.printStackTrace();
      return new MessageWrapper<AuthDto.LoginResponse>(MessageOperations.ERROR_GENERIC, null);
    }
  }

  private MessageWrapper<AuthDto.LoginResponse> handleLogin(MessageWrapper<AuthDto.LoginRequest> message) {
    var response = authService.login(message.getData().getFirst());
    return new MessageWrapper<AuthDto.LoginResponse>(MessageOperations.RESULT, java.util.List.of(response));
  }

}
