package br.ufpr.dac.usersService.config;

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

import br.ufpr.dac.shared.keys.RabbitmqConsts;

@Configuration
public class RabbitMQConfig {

  @Bean
  public Queue queue() {
    return new Queue(RabbitmqConsts.USERS_QUEUE);
  }

  @Bean
  public Exchange exchange() {
    return new DirectExchange(RabbitmqConsts.APP_EXCHANGE);
  }

  @Bean
  public Binding binding(Queue queue, Exchange exchange) {
    return BindingBuilder.bind(queue)
        .to(exchange)
        .with(RabbitmqConsts.USERS_KEY)
        .noargs();
  }

  // parte para a configuração de serialização das menssagens em json

  @Bean
  public JacksonJsonMessageConverter jsonMessageConverter() {
    var converter = new JacksonJsonMessageConverter();
    converter.setAlwaysConvertToInferredType(true);
    return converter;
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
