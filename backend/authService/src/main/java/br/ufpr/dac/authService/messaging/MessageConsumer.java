package br.ufpr.dac.authService.messaging;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import br.ufpr.dac.authService.security.JwtService;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.dto.security.LoginRequest;
import br.ufpr.dac.shared.dto.security.TokenDto;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MessageConsumer {

  private final AuthenticationManager manager;
  private final JwtService jwtService;

  @RabbitListener(queues = RabbitmqConsts.AUTH_QUEUE)
  public MessageWrapper<TokenDto> recieve(MessageWrapper<LoginRequest> message) {

    switch (message.getOperation()) {

      case MessageOperations.LOGIN -> {
        return handleLogin(message);
      }

      // case MessageOperations.LOGOUT -> {
      // handleLogout(message);
      // }

      default -> {
        System.err.println(" switch case ativou default!!!!");
        return new MessageWrapper<TokenDto>(MessageOperations.ERROR_INVALID_LOGIN, List.of());
      }

    }

  }

  public MessageWrapper<TokenDto> handleLogin(MessageWrapper<LoginRequest> message) {
    try {
      var user = message.getData().getFirst();
      System.out.println("login request: " + user);

      // o autenticador aq precisa de uma bean de passwordencoder para funcionar,
      // ela sempre supoe que a senha no banco é hasheada
      Authentication auth = manager.authenticate(
          new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

      // retorna userDetails do usuário autenticado
      UserDetails userDetails = (UserDetails) auth.getPrincipal();
      // gera o token jwt
      String token = jwtService.generateToken(userDetails);

      System.out.println(new MessageWrapper<TokenDto>(MessageOperations.RESULT, List.of(new TokenDto(token))));
      return new MessageWrapper<TokenDto>(MessageOperations.RESULT, List.of(new TokenDto(token)));

    } catch (Exception e) {
      e.printStackTrace();
      var response = new MessageWrapper<TokenDto>(MessageOperations.ERROR_INVALID_LOGIN, List.of());
      System.out.println(response);
      return response;
    }
  }

}
