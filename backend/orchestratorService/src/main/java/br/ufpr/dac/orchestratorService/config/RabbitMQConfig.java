package br.ufpr.dac.orchestratorService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  // exchange do orquestrador
  public static final String ORCHESTRATOR_EXCHANGE = "orchestrator.exchange";

  // queue para receber menssagens
  public static final String ORCHESTRATOR_QUEUE = "orchestrator.queue";

  // chave para receber menssagens
  public static final String ORCHESTRATOR_KEY = "orchestrator.queue";

  // chave para enviar menssagens para usersService
  public static final String USERS_KEY = "users.key";

  @Bean
  public Queue queue() {
    return new Queue(ORCHESTRATOR_QUEUE);
  }

  @Bean
  public Exchange exchange() {
    return new DirectExchange(ORCHESTRATOR_EXCHANGE);
  }

  @Bean
  public Binding binding(Queue queue, Exchange exchange) {
    return BindingBuilder.bind(queue)
        .to(exchange)
        .with(ORCHESTRATOR_KEY)
        .noargs();
  }

}
