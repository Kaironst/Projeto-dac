package br.ufpr.dac.authService.config;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.ufpr.dac.shared.keys.RabbitmqConsts;

@Configuration
public class RabbitMQConfig {

  @Bean
  public Exchange exchange() {
    return new DirectExchange(RabbitmqConsts.APP_EXCHANGE);
  }

  @Bean
  public Queue queue() {
    return new Queue(RabbitmqConsts.AUTH_QUEUE);
  }

  @Bean
  public Binding binding(Queue queue, Exchange exchange) {
    return BindingBuilder.bind(queue)
        .to(exchange)
        .with(RabbitmqConsts.AUTH_KEY)
        .noargs();
  }

  @Bean
  public Queue sagaQueue() {
    return new Queue(RabbitmqConsts.AUTH_SAGA_QUEUE);
  }

  @Bean
  public Binding sagaBinding(Queue sagaQueue, Exchange exchange) {
    return BindingBuilder.bind(sagaQueue)
        .to(exchange)
        .with(RabbitmqConsts.AUTH_SAGA_KEY)
        .noargs();
  }

  @Bean
  public JacksonJsonMessageConverter jsonMessageConverter() {
    var converter = new JacksonJsonMessageConverter();
    converter.setAlwaysConvertToInferredType(true);
    return converter;
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory,
      JacksonJsonMessageConverter jsonMessageConverter) {
    var template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(jsonMessageConverter);
    return template;
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      JacksonJsonMessageConverter jsonMessageConverter) {
    var factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(jsonMessageConverter);
    return factory;
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
