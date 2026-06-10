package br.ufpr.dac.authService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.authService.document.Conta;
import br.ufpr.dac.authService.document.Conta.Roles;
import br.ufpr.dac.authService.repository.ContaRepository;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;

@SpringBootApplication
@AllArgsConstructor
public class AuthServiceApplication implements CommandLineRunner {

  RabbitTemplate template;
  ContaRepository repo;
  PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) {

    // buscando os usuários
    var allCpfs = new HashSet<String>();
    repo.findAll().forEach(c -> {
      allCpfs.add(c.getCpf());
    });
    System.out.println("todos os usuários atuais:");
    allCpfs.forEach(System.out::println);

    // fazendo os objetos de busca
    var cpfClientes = new ArrayList<String>();
    cpfClientes.add("12912861012");
    cpfClientes.add("09506382000");
    cpfClientes.add("85733854057");
    cpfClientes.add("58872160006");
    cpfClientes.add("76179646090");
    var cpfGerentes = new ArrayList<String>();
    cpfGerentes.add("98574307084");
    cpfGerentes.add("64065268052");
    cpfGerentes.add("23862179060");
    cpfGerentes.add("40501740066");

    System.out.println("todos os usuários adicionados:");

    var clientes = new ArrayList<UsersDto.Cliente>();
    cpfClientes.forEach(cpf -> {
      if (!allCpfs.contains(cpf)) {
        clientes.add(UsersDto.Cliente.builder()
            .cpf(cpf)
            .build());
        System.out.println(cpf);
      }
    });
    var gerentes = new ArrayList<GerentesDto.Gerente>();
    cpfGerentes.forEach(cpf -> {
      if (!allCpfs.contains(cpf)) {
        gerentes.add(GerentesDto.Gerente.builder()
            .cpf(cpf)
            .build());
        System.out.println(cpf);
      }
    });

    // buscando os clientes finais pelo banco de dados dos outros serviços
    MessageWrapper<UsersDto.Cliente> initialClientes = null;
    MessageWrapper<GerentesDto.Gerente> initialGerentes = null;
    do {
      initialClientes = template.convertSendAndReceiveAsType(
          RabbitmqConsts.APP_EXCHANGE,
          RabbitmqConsts.USERS_KEY,
          new MessageWrapper<UsersDto.Cliente>(MessageOperations.READ_BY_CPF, clientes),
          new ParameterizedTypeReference<MessageWrapper<UsersDto.Cliente>>() {
          });
    } while (initialClientes == null);
    do {
      initialGerentes = template.convertSendAndReceiveAsType(
          RabbitmqConsts.APP_EXCHANGE,
          RabbitmqConsts.GERENTES_KEY,
          new MessageWrapper<GerentesDto.Gerente>(MessageOperations.READ_BY_CPF, gerentes),
          new ParameterizedTypeReference<MessageWrapper<GerentesDto.Gerente>>() {
          });

    } while (initialGerentes == null);

    // monta os dados para inserir no repositório.
    initialClientes.getData().forEach(c -> {
      var conta = Conta.builder()
          .userId(c.getId())
          .email(c.getEmail())
          .cpf(c.getCpf())
          .senha(passwordEncoder.encode("1234"))
          .roles(List.of(Roles.ROLE_CLIENTE))
          .build();
      repo.save(conta);
    });

    initialGerentes.getData().forEach(c -> {
      var conta = Conta.builder()
          .userId(c.getId())
          .email(c.getEmail())
          .cpf(c.getCpf())
          .senha(passwordEncoder.encode("1234"))
          .roles(c.getAdministrador() ? List.of(Roles.ROLE_GERENTE, Roles.ROLE_ADMINISTRADOR)
              : List.of(Roles.ROLE_GERENTE))
          .build();
      repo.save(conta);
    });

  }

  public static void main(String[] args) {
    SpringApplication.run(AuthServiceApplication.class, args);
  }

}
