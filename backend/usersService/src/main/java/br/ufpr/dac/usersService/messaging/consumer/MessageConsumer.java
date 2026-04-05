package br.ufpr.dac.usersService.messaging.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.usersService.config.RabbitMQConfig;
import br.ufpr.dac.usersService.entity.Cliente;
import br.ufpr.dac.usersService.entity.Endereco;
import br.ufpr.dac.usersService.messaging.dto.UsersDto;
import br.ufpr.dac.usersService.repository.ClienteRepository;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MessageConsumer {

  private final ClienteRepository repo;

  @RabbitListener(queues = RabbitMQConfig.USERS_QUEUE)
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
    clientes.forEach(cliente -> {

      var clienteAtual = repo.findById(cliente.getId()).orElseThrow();

      clienteAtual.setNome(cliente.getNome());
      clienteAtual.setCpf(cliente.getCpf());
      clienteAtual.setEmail(cliente.getEmail());
      clienteAtual.setEstado(cliente.getEstado());
      clienteAtual.setTelefone(cliente.getTelefone());
      clienteAtual.setSalario(cliente.getSalario());

      // atualiza endereços
      Map<Long, Endereco> enderecosExistentes = clienteAtual.getEnderecos()
          .stream()
          .collect(Collectors.toMap(e -> e.getId(), e -> e));

      List<Endereco> enderecosAtualizados = new ArrayList<>();

      if (cliente.getEnderecos() != null)
        cliente.getEnderecos().forEach(endereco -> {
          // atualiza endereco existente
          if (endereco.getId() != 0 && enderecosExistentes.containsKey(endereco.getId())) {
            var enderecoAtual = enderecosExistentes.get(endereco.getId());

            enderecoAtual.setLogradouro(endereco.getLogradouro());
            enderecoAtual.setNumero(endereco.getNumero());
            enderecoAtual.setComplemento(endereco.getComplemento());
            enderecoAtual.setCep(endereco.getCep());
            enderecoAtual.setCidade(endereco.getCidade());
            enderecoAtual.setEstado(endereco.getEstado());

            enderecosAtualizados.add(enderecoAtual);
            enderecosExistentes.remove(endereco.getId());
          }
          // cria novo endereco
          else {
            var enderecoNovo = new Endereco();

            enderecoNovo.setLogradouro(endereco.getLogradouro());
            enderecoNovo.setNumero(endereco.getNumero());
            enderecoNovo.setComplemento(endereco.getComplemento());
            enderecoNovo.setCep(endereco.getCep());
            enderecoNovo.setCidade(endereco.getCidade());
            enderecoNovo.setEstado(endereco.getEstado());
            enderecoNovo.setCliente(clienteAtual);

            enderecosAtualizados.add(enderecoNovo);
          }
        });

      // remove enderecos desligados manualmente
      enderecosExistentes.values().forEach(enderecoRemovido -> {
        clienteAtual.getEnderecos().remove(enderecoRemovido);
      });

      // troca a lista
      clienteAtual.getEnderecos().clear();
      clienteAtual.getEnderecos().addAll(enderecosAtualizados);

      clientesAtualizados.add(clienteAtual);

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
