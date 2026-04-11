package br.ufpr.dac.usersService.messaging.consumer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.dto.keys.RabbitmqConsts;
import br.ufpr.dac.usersService.entity.Cliente;
import br.ufpr.dac.usersService.entity.Endereco;
import br.ufpr.dac.usersService.repository.ClienteRepository;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MessageConsumer {

  private final ClienteRepository repo;

  @RabbitListener(queues = RabbitmqConsts.USERS_QUEUE)
  public UsersDto.Message recieve(UsersDto.Message message) {
    try {
      switch (message.getOperation()) {
        case "CREATE" -> {
          return handleCreate(message.getData());
        }
        case "READ" -> {
          return handleRead(message.getData());
        }
        case "READ_ALL" -> {
          return handleReadAll();
        }
        case "UPDATE" -> {
          return handleUpdate(message.getData());
        }
        case "DELETE" -> {
          return handleDelete(message.getData());
        }
        default -> {
          throw new UnsupportedOperationException();
        }
      }
    } catch (Exception e) {
      System.out.println("error on message consumer listener");
      e.printStackTrace();
      return new UsersDto.Message("ERROR", null);
    }
  }

  private static List<UsersDto.Cliente> clientesToDto(List<Cliente> clientes) {
    var clientesDto = new ArrayList<UsersDto.Cliente>();

    clientes.forEach(cliente -> {
      var novoClienteDto = UsersDto.Cliente.builder()
          .id(cliente.getId())
          .salario(cliente.getSalario())
          .nome(cliente.getNome())
          .email(cliente.getEmail())
          .cpf(cliente.getCpf())
          .estado(cliente.getEstado())
          .build();

      var enderecosCliente = new ArrayList<UsersDto.Endereco>();
      if (cliente.getEnderecos() != null)
        cliente.getEnderecos().forEach(endereco -> {
          var novoEndereco = UsersDto.Endereco.builder()
              .id(endereco.getId())
              .logradouro(endereco.getLogradouro())
              .numero(endereco.getNumero())
              .cidade(endereco.getCidade())
              .cep(endereco.getCep())
              .complemento(endereco.getComplemento())
              .estado(endereco.getEstado())
              .build();
          enderecosCliente.add(novoEndereco);
        });
      novoClienteDto.setEnderecos(enderecosCliente);
      clientesDto.add(novoClienteDto);
    });

    return clientesDto;

  }

  private static List<Cliente> dtoToClientes(List<UsersDto.Cliente> clientesDto) {
    var clientes = new ArrayList<Cliente>();

    clientesDto.forEach(clienteDto -> {
      var novoCliente = Cliente.builder()
          .id(clienteDto.getId())
          .salario(clienteDto.getSalario())
          .nome(clienteDto.getNome())
          .email(clienteDto.getEmail())
          .cpf(clienteDto.getCpf())
          .estado(clienteDto.getEstado())
          .build();

      var enderecosCliente = new ArrayList<Endereco>();
      if (clienteDto.getEnderecos() != null)
        clienteDto.getEnderecos().forEach(endereco -> {
          var novoEndereco = Endereco.builder()
              .id(endereco.getId())
              .logradouro(endereco.getLogradouro())
              .numero(endereco.getNumero())
              .cidade(endereco.getCidade())
              .cep(endereco.getCep())
              .complemento(endereco.getComplemento())
              .estado(endereco.getEstado())
              .cliente(novoCliente)
              .build();
          enderecosCliente.add(novoEndereco);
        });
      novoCliente.setEnderecos(enderecosCliente);
      clientes.add(novoCliente);
    });

    return clientes;

  }

  @Transactional
  private UsersDto.Message handleCreate(List<UsersDto.Cliente> clientes) {
    List<Cliente> qResult = repo.saveAll(dtoToClientes(clientes));
    return new UsersDto.Message("RESULT", clientesToDto(qResult));
  }

  @Transactional(readOnly = true)
  private UsersDto.Message handleRead(List<UsersDto.Cliente> clientes) {
    var idList = new ArrayList<Long>();
    clientes.forEach(c -> idList.add(c.getId()));

    List<Cliente> clientesEncontrados = repo.findAllById(idList);
    return new UsersDto.Message("RESULT", clientesToDto(clientesEncontrados));
  }

  @Transactional(readOnly = true)
  private UsersDto.Message handleReadAll() {
    var clientesEncontrados = repo.findAll();

    return new UsersDto.Message("RESULT", clientesToDto(clientesEncontrados));

  }

  @Transactional
  private UsersDto.Message handleUpdate(List<UsersDto.Cliente> clientes) {
    List<Cliente> clientesAtualizados = new ArrayList<>();
    dtoToClientes(clientes).forEach(cliente -> {

      System.out.println(cliente);

      var clienteAtual = repo.findById(cliente.getId()).orElseThrow();

      clienteAtual.setNome(cliente.getNome());
      clienteAtual.setCpf(cliente.getCpf());
      clienteAtual.setEmail(cliente.getEmail());
      clienteAtual.setEstado(cliente.getEstado());
      clienteAtual.setTelefone(cliente.getTelefone());
      clienteAtual.setSalario(cliente.getSalario());
      clienteAtual.setEnderecos(cliente.getEnderecos());

      clientesAtualizados.add(repo.save(clienteAtual));

    });
    return new UsersDto.Message("RESULT", clientesToDto(clientesAtualizados));
  }

  @Transactional
  private UsersDto.Message handleDelete(List<UsersDto.Cliente> clientes) {
    clientes.forEach(cliente -> {
      repo.deleteById(cliente.getId());
    });
    return new UsersDto.Message("RESULT", null);
  }

}
