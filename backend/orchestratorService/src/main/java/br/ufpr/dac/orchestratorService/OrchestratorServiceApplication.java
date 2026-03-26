package br.ufpr.dac.orchestratorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import br.ufpr.dac.orchestratorService.messaging.producer.Producer;

@SpringBootApplication
public class OrchestratorServiceApplication {

  @Autowired
  Producer producer;

  public static void main(String[] args) {
    SpringApplication.run(OrchestratorServiceApplication.class, args);

  }

  @Bean
  CommandLineRunner menssagemTeste() {
    return (args) -> {
      producer.messageUsers("menssagemTeste");
    };
  }

}
