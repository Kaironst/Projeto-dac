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
public class Consumer {

  private final ClienteRepository repo;

  @RabbitListener(queues = RabbitMQConfig.USERS_QUEUE)
  public void recieve(UsersDto.Message message) {
    switch (message.getOperation()) {
      case "CREATE" -> {
        handleCreate(message.getData());
      }
      case "READ" -> {
        handleRead(message.getData());
      }
      case "UPDATE" -> {
        handleUpdate(message.getData());
      }
      case "DELETE" -> {
        handleDelete(message.getData());
      }
    }
  }

  @Transactional
  private void handleCreate(List<UsersDto.Cliente> clientes) {
    clientes.forEach(cliente -> {
      var novoCliente = Cliente.builder()
          .salario(cliente.getSalario())
          .nome(cliente.getNome())
          .email(cliente.getEmail())
          .cpf(cliente.getCpf())
          .estado(cliente.getEstado())
          .build();

      var enderecosCliente = new ArrayList<Endereco>();
      cliente.getEnderecos().forEach(endereco -> {
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

      repo.save(novoCliente);
    });
  }

  @Transactional(readOnly = true)
  private void handleRead(List<UsersDto.Cliente> clientes) {
    List<Cliente> clientesEncontrados = new ArrayList<>();

    clientes.forEach(cliente -> {
      clientesEncontrados.add(repo.findById(cliente.getId()).orElseThrow());
    });

    // producer.returnReadMessage(clientesEncontrados);

  }

  @Transactional(readOnly = true)
  private void handleReadAll() {
    var clientesEncontrados = repo.findAll();

    // producer.returnReadMessage(clientesEncontrados);

  }

  @Transactional
  private void handleUpdate(List<UsersDto.Cliente> clientes) {
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

    });
  }

  @Transactional
  private void handleDelete(List<UsersDto.Cliente> clientes) {
    clientes.forEach(cliente -> {
      repo.deleteById(cliente.getId());
    });
  }

}
