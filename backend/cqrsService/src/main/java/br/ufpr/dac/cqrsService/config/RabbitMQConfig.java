package br.ufpr.dac.cqrsService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
  public Exchange debeziumExchange() {
    return new TopicExchange("debezium.exchange");
  }

  @Bean
  public Queue debeziumQueue() {
    return new Queue(RabbitmqConsts.CQRS_DEBEZIUM_QUEUE);
  }

  @Bean
  public Binding debeziumBinding(Queue debeziumQueue, Exchange debeziumExchange) {
    return BindingBuilder.bind(debeziumQueue)
        .to(debeziumExchange)
        .with("#")
        .noargs();
  }

  @Bean
  public Exchange requestExchange() {
    return new DirectExchange(RabbitmqConsts.APP_EXCHANGE);
  }

  @Bean
  public Queue requestQueue() {
    return new Queue(RabbitmqConsts.CQRS_REQUEST_QUEUE);
  }

  @Bean
  public Binding requestBinding(Queue requestQueue, Exchange requestExchange) {
    return BindingBuilder.bind(requestQueue)
        .to(requestExchange)
        .with(RabbitmqConsts.CQRS_REQUEST_KEY)
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
