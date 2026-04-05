package br.ufpr.dac.orchestratorService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OrchestratorServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrchestratorServiceApplication.class, args);

  }

  @Bean
  CommandLineRunner menssagemTeste() {
    return (args) -> {
    };
  }

}
