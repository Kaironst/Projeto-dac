package br.ufpr.dac.orchestratorService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.var;

@Configuration
public class RabbitMQConfig {

  // exchange do orquestrador
  public static final String APP_EXCHANGE = "app.exchange";
  // queue para receber menssagens
  public static final String ORCHESTRATOR_QUEUE = "orchestrator.queue";
  // chave para receber menssagens
  public static final String ORCHESTRATOR_KEY = "orchestrator.key";
  // chave para enviar menssagens para usersService
  public static final String USERS_KEY = "users.key";
  // chave para enviar menssagens para apiGatewayService
  public static final String API_GATEWAY_KEY = "apiGateway.key";

  @Bean
  public Queue queue() {
    return new Queue(ORCHESTRATOR_QUEUE);
  }

  @Bean
  public Exchange exchange() {
    return new DirectExchange(APP_EXCHANGE);
  }

  @Bean
  public Binding binding(Queue queue, Exchange exchange) {
    return BindingBuilder.bind(queue)
        .to(exchange)
        .with(ORCHESTRATOR_KEY)
        .noargs();
  }

  // parte para a configuração de serialização das menssagens em json

  @Bean
  public JacksonJsonMessageConverter jsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }

  // sobrescreve o rabbit template para utilizar a serialização em json
  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory,
      JacksonJsonMessageConverter jsonMessageConverter) {
    var template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(jsonMessageConverter);
    return template;
  }

  // sobrescreve o rabbit listener padrão para usar serialização json
  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      JacksonJsonMessageConverter jsonMessageConverter) {
    var factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(jsonMessageConverter);
    return factory;
  }

}
