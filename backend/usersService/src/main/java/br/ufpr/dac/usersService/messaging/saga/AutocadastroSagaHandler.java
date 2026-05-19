package br.ufpr.dac.usersService.messaging.saga;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.dac.shared.dto.AutocadastroDto;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.dto.UsersDto;
import br.ufpr.dac.shared.dto.AutocadastroDto.StatusSolicitacao;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.usersService.entity.Cliente;
import br.ufpr.dac.usersService.entity.Endereco;
import br.ufpr.dac.usersService.entity.EnderecoSolicitacao;
import br.ufpr.dac.usersService.entity.SolicitacaoAutocadastro;
import br.ufpr.dac.usersService.repository.ClienteRepository;
import br.ufpr.dac.usersService.repository.SolicitacaoAutocadastroRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AutocadastroSagaHandler {

  private final RabbitTemplate template;
  private final ClienteRepository clienteRepo;
  private final SolicitacaoAutocadastroRepository solicitacaoRepo;

  @Transactional(readOnly = true)
  public MessageWrapper<AutocadastroDto.Solicitacao> handleListarPendentes() {
    var pendentes = solicitacaoRepo.findAllByStatus(StatusSolicitacao.PENDENTE)
        .stream()
        .map(this::toDto)
        .toList();

    return new MessageWrapper<AutocadastroDto.Solicitacao>(MessageOperations.RESULT, pendentes);
  }

  @Transactional(readOnly = true)
  public void handleValidarCpf(SagaMessageWrapper<AutocadastroDto.SolicitacaoEntrada> message) {
    boolean cpfDisponivel = true;

    try {
      var cliente = message.getData().getFirst().getCliente();
      cpfDisponivel = cliente.getCpf() != null
          && clienteRepo.findByCpf(cliente.getCpf()) == null
          && !solicitacaoRepo.existsByCpfAndStatus(cliente.getCpf(), StatusSolicitacao.PENDENTE);
    } catch (Exception e) {
      e.printStackTrace();
      cpfDisponivel = false;
    }

    enviarMensagem(new SagaMessageWrapper<AutocadastroDto.SolicitacaoEntrada>(
        cpfDisponivel
            ? SagaOperations.Autocadastro.VALIDAR_CPF_RESULT
            : SagaOperations.Autocadastro.VALIDAR_CPF_ERROR,
        cpfDisponivel ? message.getData() : List.of(),
        message.getCorrelationId()));
  }

  @Transactional
  public void handleRegistrarSolicitacao(SagaMessageWrapper<AutocadastroDto.SolicitacaoEntrada> message) {
    try {
      var entity = entradaToEntity(message.getData().getFirst());
      var solicitacao = solicitacaoRepo.save(entity);

      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
          SagaOperations.Autocadastro.REGISTRAR_SOLICITACAO_RESULT,
          List.of(toDto(solicitacao)),
          message.getCorrelationId()));
    } catch (Exception e) {
      e.printStackTrace();
      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
          SagaOperations.Autocadastro.REGISTRAR_SOLICITACAO_ERROR,
          List.of(),
          message.getCorrelationId()));
    }
  }

  @Transactional
  public void handleVincularGerente(SagaMessageWrapper<AutocadastroDto.Solicitacao> message) {
    try {
      var solicitacaoDto = message.getData().getFirst();
      var solicitacao = solicitacaoRepo.findById(solicitacaoDto.getId()).orElseThrow();

      solicitacao.setGerente(solicitacaoDto.getGerente().getId());
      var atualizada = solicitacaoRepo.save(solicitacao);

      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
          SagaOperations.Autocadastro.VINCULAR_GERENTE_RESULT,
          List.of(toDto(atualizada)),
          message.getCorrelationId()));
    } catch (Exception e) {
      e.printStackTrace();
      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
          SagaOperations.Autocadastro.VINCULAR_GERENTE_ERROR,
          List.of(),
          message.getCorrelationId()));
    }
  }

  @Transactional
  public void handleAprovarSolicitacao(SagaMessageWrapper<AutocadastroDto.Aprovacao> message) {
    try {
      var aprovacao = message.getData().getFirst();
      var solicitacao = findSolicitacaoPendente(aprovacao);

      if (clienteRepo.findByCpf(solicitacao.getCpf()) != null) {
        throw new IllegalStateException("CPF ja possui cliente ativo.");
      }

      if (aprovacao.getGerenteId() != null) {
        solicitacao.setGerente(aprovacao.getGerenteId());
      }

      if (solicitacao.getGerente() == null) {
        throw new IllegalStateException("Solicitacao sem gerente responsavel.");
      }

      var clienteCriado = clienteRepo.save(solicitacaoToCliente(solicitacao));
      solicitacao.setCliente(clienteCriado.getId());
      solicitacao.setStatus(StatusSolicitacao.APROVADO);
      solicitacao.setMotivoRejeicao(null);
      solicitacao.setDataAnalise(LocalDateTime.now());

      var atualizada = solicitacaoRepo.save(solicitacao);

      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
          SagaOperations.Autocadastro.APROVAR_SOLICITACAO_RESULT,
          List.of(toDto(atualizada)),
          message.getCorrelationId()));
    } catch (Exception e) {
      e.printStackTrace();
      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
          SagaOperations.Autocadastro.APROVAR_SOLICITACAO_ERROR,
          List.of(),
          message.getCorrelationId()));
    }
  }

  @Transactional
  public void handleRejeitarSolicitacao(SagaMessageWrapper<AutocadastroDto.Rejeicao> message) {
    try {
      var rejeicao = message.getData().getFirst();
      var motivo = rejeicao.getMotivo() == null ? "" : rejeicao.getMotivo().trim();

      if (motivo.isEmpty()) {
        throw new IllegalArgumentException("Motivo de rejeicao e obrigatorio.");
      }

      var solicitacao = solicitacaoRepo.findById(rejeicao.getSolicitacaoId())
          .filter(item -> item.getStatus() == StatusSolicitacao.PENDENTE)
          .orElseThrow();

      if (rejeicao.getGerenteId() != null) {
        solicitacao.setGerente(rejeicao.getGerenteId());
      }

      solicitacao.setStatus(StatusSolicitacao.REJEITADO);
      solicitacao.setMotivoRejeicao(motivo);
      solicitacao.setDataAnalise(LocalDateTime.now());

      var atualizada = solicitacaoRepo.save(solicitacao);

      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
          SagaOperations.Autocadastro.REJEITAR_SOLICITACAO_RESULT,
          List.of(toDto(atualizada)),
          message.getCorrelationId()));
    } catch (Exception e) {
      e.printStackTrace();
      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
          SagaOperations.Autocadastro.REJEITAR_SOLICITACAO_ERROR,
          List.of(),
          message.getCorrelationId()));
    }
  }

  @Transactional
  public void handleRollbackSolicitacao(SagaMessageWrapper<AutocadastroDto.Solicitacao> message) {
    try {
      if (message.getData() != null && !message.getData().isEmpty() && message.getData().getFirst().getId() != null) {
        var solicitacaoDto = message.getData().getFirst();
        var solicitacao = solicitacaoRepo.findById(solicitacaoDto.getId()).orElseThrow();
        var clienteId = solicitacaoDto.getCliente() != null && solicitacaoDto.getCliente().getId() != null
            ? solicitacaoDto.getCliente().getId()
            : solicitacao.getCliente();

        if (clienteId != null && clienteRepo.existsById(clienteId)) {
          clienteRepo.deleteById(clienteId);
          solicitacao.setCliente(null);
        }

        solicitacao.setStatus(StatusSolicitacao.FALHA);
        solicitacaoRepo.save(solicitacao);
      }

      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
          SagaOperations.Autocadastro.ROLLBACK_SOLICITACAO_RESULT,
          List.of(),
          message.getCorrelationId()));
    } catch (Exception e) {
      e.printStackTrace();
      enviarMensagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
          SagaOperations.Autocadastro.ROLLBACK_SOLICITACAO_ERROR,
          List.of(),
          message.getCorrelationId()));
    }
  }

  private SolicitacaoAutocadastro entradaToEntity(AutocadastroDto.SolicitacaoEntrada entrada) {
    var cliente = entrada.getCliente();
    var solicitacao = SolicitacaoAutocadastro.builder()
        .nome(cliente.getNome())
        .email(cliente.getEmail())
        .cpf(cliente.getCpf())
        .estado(cliente.getEstado())
        .telefone(cliente.getTelefone())
        .salario(cliente.getSalario())
        .status(StatusSolicitacao.PENDENTE)
        .dataSolicitacao(LocalDateTime.now())
        .build();

    var enderecos = new ArrayList<EnderecoSolicitacao>();
    if (cliente.getEnderecos() != null) {
      cliente.getEnderecos().forEach(endereco -> enderecos.add(EnderecoSolicitacao.builder()
          .logradouro(endereco.getLogradouro())
          .numero(endereco.getNumero())
          .complemento(endereco.getComplemento())
          .cep(endereco.getCep())
          .cidade(endereco.getCidade())
          .estado(endereco.getEstado())
          .solicitacao(solicitacao)
          .build()));
    }

    solicitacao.setEnderecos(enderecos);
    return solicitacao;
  }

  private Cliente solicitacaoToCliente(SolicitacaoAutocadastro solicitacao) {
    var cliente = Cliente.builder()
        .nome(solicitacao.getNome())
        .email(solicitacao.getEmail())
        .cpf(solicitacao.getCpf())
        .estado(solicitacao.getEstado())
        .telefone(solicitacao.getTelefone())
        .salario(solicitacao.getSalario())
        .build();

    var enderecos = new ArrayList<Endereco>();
    if (solicitacao.getEnderecos() != null) {
      solicitacao.getEnderecos().forEach(endereco -> enderecos.add(Endereco.builder()
          .logradouro(endereco.getLogradouro())
          .numero(endereco.getNumero())
          .complemento(endereco.getComplemento())
          .cep(endereco.getCep())
          .cidade(endereco.getCidade())
          .estado(endereco.getEstado())
          .cliente(cliente)
          .build()));
    }

    cliente.setEnderecos(enderecos);
    return cliente;
  }

  private SolicitacaoAutocadastro findSolicitacaoPendente(AutocadastroDto.Aprovacao aprovacao) {
    if (aprovacao.getSolicitacaoId() != null) {
      return solicitacaoRepo.findById(aprovacao.getSolicitacaoId())
          .filter(solicitacao -> solicitacao.getStatus() == StatusSolicitacao.PENDENTE)
          .orElseThrow();
    }

    if (aprovacao.getCpf() != null) {
      return solicitacaoRepo.findFirstByCpfAndStatus(aprovacao.getCpf(), StatusSolicitacao.PENDENTE)
          .orElseThrow();
    }

    throw new IllegalArgumentException("Informe solicitacaoId ou CPF para aprovar autocadastro.");
  }

  private AutocadastroDto.Solicitacao toDto(SolicitacaoAutocadastro solicitacao) {
    var cliente = UsersDto.Cliente.builder()
        .id(solicitacao.getCliente())
        .nome(solicitacao.getNome())
        .email(solicitacao.getEmail())
        .cpf(solicitacao.getCpf())
        .estado(solicitacao.getEstado())
        .telefone(solicitacao.getTelefone())
        .salario(solicitacao.getSalario())
        .enderecos(toEnderecosDto(solicitacao.getEnderecos()))
        .build();

    var gerente = solicitacao.getGerente() == null
        ? null
        : GerentesDto.Gerente.builder().id(solicitacao.getGerente()).build();

    return AutocadastroDto.Solicitacao.builder()
        .id(solicitacao.getId())
        .cliente(cliente)
        .gerente(gerente)
        .status(solicitacao.getStatus())
        .motivoRejeicao(solicitacao.getMotivoRejeicao())
        .dataSolicitacao(solicitacao.getDataSolicitacao())
        .dataAnalise(solicitacao.getDataAnalise())
        .build();
  }

  private List<UsersDto.Endereco> toEnderecosDto(List<EnderecoSolicitacao> enderecos) {
    var enderecosDto = new ArrayList<UsersDto.Endereco>();
    if (enderecos == null) {
      return enderecosDto;
    }

    enderecos.forEach(endereco -> enderecosDto.add(UsersDto.Endereco.builder()
        .id(endereco.getId())
        .logradouro(endereco.getLogradouro())
        .numero(endereco.getNumero())
        .complemento(endereco.getComplemento())
        .cep(endereco.getCep())
        .cidade(endereco.getCidade())
        .estado(endereco.getEstado())
        .build()));
    return enderecosDto;
  }

  private <T> void enviarMensagem(SagaMessageWrapper<T> message) {
    template.convertAndSend(
        RabbitmqConsts.APP_EXCHANGE,
        RabbitmqConsts.ORCHESTRATOR_SAGA_KEY,
        message);
  }

}
