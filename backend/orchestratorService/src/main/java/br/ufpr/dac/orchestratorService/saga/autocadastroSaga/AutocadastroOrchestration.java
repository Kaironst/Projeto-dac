package br.ufpr.dac.orchestratorService.saga.autocadastroSaga;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.messaging.producer.GerentesProducer;
import br.ufpr.dac.orchestratorService.saga.SagaProducerFactory;
import br.ufpr.dac.orchestratorService.saga.SagaState;
import br.ufpr.dac.orchestratorService.saga.SagaStatus;
import br.ufpr.dac.orchestratorService.saga.SagaProducerFactory.SagaProducer;
import br.ufpr.dac.shared.dto.AutocadastroDto;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.MessageWrapper;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.MessageOperations;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor
public class AutocadastroOrchestration {

  private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
  private static final SecureRandom RANDOM = new SecureRandom();

  @Getter
  private final Map<UUID, SagaState<AutocadastroData>> sagas = new ConcurrentHashMap<>();
  private final SagaProducerFactory producerFactory;
  private final GerentesProducer gerentesProducer;
  private final Set<String> errors = Set.of(
      SagaOperations.Autocadastro.VALIDAR_CPF_ERROR,
      SagaOperations.Autocadastro.REGISTRAR_SOLICITACAO_ERROR,
      SagaOperations.Autocadastro.ESCOLHER_GERENTE_ERROR,
      SagaOperations.Autocadastro.VINCULAR_GERENTE_ERROR,
      SagaOperations.Autocadastro.APROVAR_SOLICITACAO_ERROR,
      SagaOperations.Autocadastro.CRIAR_CONTA_ERROR,
      SagaOperations.Autocadastro.CRIAR_AUTH_ERROR,
      SagaOperations.Autocadastro.ENVIAR_EMAIL_APROVACAO_ERROR,
      SagaOperations.Autocadastro.ROLLBACK_SOLICITACAO_ERROR,
      SagaOperations.Autocadastro.ROLLBACK_CONTA_ERROR,
      SagaOperations.Autocadastro.ROLLBACK_AUTH_ERROR,
      MessageOperations.ERROR_GENERIC);

  public void startSaga(SagaMessageWrapper<AutocadastroDto.SolicitacaoEntrada> message) {
    UUID correlationId = UUID.randomUUID();
    message.setCorrelationId(correlationId);

    var state = new SagaState<AutocadastroData>(
        correlationId,
        AutocadastroPasso.VALIDANDO_CPF,
        SagaStatus.RUNNING,
        new AutocadastroData());

    state.getSagaData().setEntrada(message.getData().getFirst());
    state.getSagaData().setCliente(message.getData().getFirst().getCliente());
    sagas.put(correlationId, state);

    SagaProducer<AutocadastroDto.SolicitacaoEntrada> producer = producerFactory.create();
    producer.enviarMenssagem(new SagaMessageWrapper<AutocadastroDto.SolicitacaoEntrada>(
        SagaOperations.Autocadastro.VALIDAR_CPF,
        message.getData(),
        correlationId),
        RabbitmqConsts.USERS_SAGA_KEY);
  }

  public void handleCpfValidado(SagaMessageWrapper<AutocadastroDto.SolicitacaoEntrada> message) {
    var state = sagas.get(message.getCorrelationId());
    if (state == null) {
      return;
    }

    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(AutocadastroPasso.REGISTRANDO_SOLICITACAO);

    SagaProducer<AutocadastroDto.SolicitacaoEntrada> producer = producerFactory.create();
    producer.enviarMenssagem(new SagaMessageWrapper<AutocadastroDto.SolicitacaoEntrada>(
        SagaOperations.Autocadastro.REGISTRAR_SOLICITACAO,
        List.of(state.getSagaData().getEntrada()),
        message.getCorrelationId()),
        RabbitmqConsts.USERS_SAGA_KEY);
  }

  public void handleSolicitacaoRegistrada(SagaMessageWrapper<AutocadastroDto.Solicitacao> message) {
    var state = sagas.get(message.getCorrelationId());
    if (state == null) {
      return;
    }

    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(AutocadastroPasso.ESCOLHENDO_GERENTE);
    state.getSagaData().setSolicitacao(message.getData().getFirst());

    var gerentesMessage = gerentesProducer.enviarMenssagem(
        new MessageWrapper<GerentesDto.Gerente>(MessageOperations.READ_ALL, null));

    if (gerentesMessage == null || gerentesMessage.getData() == null) {
      handleRollback(state);
      return;
    }

    var gerentesIds = gerentesMessage.getData().stream()
        .filter(gerente -> !Boolean.TRUE.equals(gerente.getAdministrador()))
        .map(GerentesDto.Gerente::getId)
        .sorted()
        .toList();

    if (gerentesIds.isEmpty()) {
      handleRollback(state);
      return;
    }

    SagaProducer<Long> producer = producerFactory.create();
    producer.enviarMenssagem(new SagaMessageWrapper<Long>(
        SagaOperations.Autocadastro.ESCOLHER_GERENTE,
        gerentesIds,
        message.getCorrelationId()),
        RabbitmqConsts.CONTAS_SAGA_KEY);
  }

  public void handleGerenteEscolhido(SagaMessageWrapper<Long> message) {
    var state = sagas.get(message.getCorrelationId());
    if (state == null) {
      return;
    }

    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(AutocadastroPasso.VINCULANDO_GERENTE);
    var gerente = GerentesDto.Gerente.builder().id(message.getData().getFirst()).build();
    state.getSagaData().setGerenteResponsavel(gerente);
    state.getSagaData().getSolicitacao().setGerente(gerente);

    SagaProducer<AutocadastroDto.Solicitacao> producer = producerFactory.create();
    producer.enviarMenssagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
        SagaOperations.Autocadastro.VINCULAR_GERENTE,
        List.of(state.getSagaData().getSolicitacao()),
        message.getCorrelationId()),
        RabbitmqConsts.USERS_SAGA_KEY);
  }

  public void handleGerenteVinculado(SagaMessageWrapper<AutocadastroDto.Solicitacao> message) {
    var state = sagas.get(message.getCorrelationId());
    if (state == null) {
      return;
    }

    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(AutocadastroPasso.AGUARDANDO_APROVACAO);
    state.setStatus(SagaStatus.SUCCESS);
    state.getSagaData().setSolicitacao(message.getData().getFirst());
    sagas.remove(message.getCorrelationId());
  }

  public void startAprovacaoSaga(SagaMessageWrapper<AutocadastroDto.Aprovacao> message) {
    UUID correlationId = UUID.randomUUID();
    message.setCorrelationId(correlationId);

    var state = new SagaState<AutocadastroData>(
        correlationId,
        AutocadastroPasso.APROVANDO_SOLICITACAO,
        SagaStatus.RUNNING,
        new AutocadastroData());

    state.getSagaData().setAprovacao(message.getData().getFirst());
    sagas.put(correlationId, state);

    SagaProducer<AutocadastroDto.Aprovacao> producer = producerFactory.create();
    producer.enviarMenssagem(new SagaMessageWrapper<AutocadastroDto.Aprovacao>(
        SagaOperations.Autocadastro.APROVAR_SOLICITACAO,
        message.getData(),
        correlationId),
        RabbitmqConsts.USERS_SAGA_KEY);
  }

  public void handleSolicitacaoAprovada(SagaMessageWrapper<AutocadastroDto.Solicitacao> message) {
    var state = sagas.get(message.getCorrelationId());
    if (state == null) {
      return;
    }

    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(AutocadastroPasso.CRIANDO_CONTA);
    state.getSagaData().setSolicitacao(message.getData().getFirst());

    SagaProducer<AutocadastroDto.Solicitacao> producer = producerFactory.create();
    producer.enviarMenssagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
        SagaOperations.Autocadastro.CRIAR_CONTA,
        List.of(state.getSagaData().getSolicitacao()),
        message.getCorrelationId()),
        RabbitmqConsts.CONTAS_SAGA_KEY);
  }

  public void handleContaCriada(SagaMessageWrapper<AutocadastroDto.ContaCriada> message) {
    var state = sagas.get(message.getCorrelationId());
    if (state == null) {
      return;
    }

    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.getSagaData().setContaCriada(message.getData().getFirst().getConta());

    var senhaTemporaria = gerarSenhaTemporaria();
    state.getSagaData().setUsuarioAuth(AutocadastroDto.UsuarioAuth.builder()
        .solicitacaoId(state.getSagaData().getSolicitacao().getId())
        .clienteId(state.getSagaData().getSolicitacao().getCliente().getId())
        .email(state.getSagaData().getSolicitacao().getCliente().getEmail())
        .senhaTemporaria(senhaTemporaria)
        .tipo("CLIENTE")
        .build());

    state.setStep(AutocadastroPasso.CRIANDO_AUTH);
    SagaProducer<AutocadastroDto.UsuarioAuth> producer = producerFactory.create();
    producer.enviarMenssagem(new SagaMessageWrapper<AutocadastroDto.UsuarioAuth>(
        SagaOperations.Autocadastro.CRIAR_AUTH,
        List.of(state.getSagaData().getUsuarioAuth()),
        message.getCorrelationId()),
        RabbitmqConsts.AUTH_SAGA_KEY);
  }

  public void handleAuthCriado(SagaMessageWrapper<AutocadastroDto.UsuarioAuth> message) {
    var state = sagas.get(message.getCorrelationId());
    if (state == null) {
      return;
    }

    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.getSagaData().setAuthCriado(true);
    state.setStep(AutocadastroPasso.ENVIANDO_EMAIL_APROVACAO);
    SagaProducer<AutocadastroDto.Notificacao> producer = producerFactory.create();
    producer.enviarMenssagem(new SagaMessageWrapper<AutocadastroDto.Notificacao>(
        SagaOperations.Autocadastro.ENVIAR_EMAIL_APROVACAO,
        List.of(criarNotificacaoAprovacao(state)),
        message.getCorrelationId()),
        RabbitmqConsts.MAIL_SAGA_KEY);
  }

  public void handleEmailAprovacaoEnviado(SagaMessageWrapper<AutocadastroDto.Notificacao> message) {
    var state = sagas.get(message.getCorrelationId());
    if (state == null) {
      return;
    }

    if (errors.contains(message.getOperation())) {
      handleRollback(state);
      return;
    }

    state.setStep(AutocadastroPasso.FINALIZADO);
    state.setStatus(SagaStatus.SUCCESS);
    sagas.remove(message.getCorrelationId());
  }

  private void handleRollback(SagaState<AutocadastroData> state) {
    state.setStatus(SagaStatus.COMPENSATING);

    if (state.getSagaData().isAuthCriado() && state.getSagaData().getUsuarioAuth() != null) {
      state.setStep(AutocadastroPasso.COMPENSANDO_AUTH);
      SagaProducer<AutocadastroDto.UsuarioAuth> producer = producerFactory.create();
      producer.enviarMenssagem(new SagaMessageWrapper<AutocadastroDto.UsuarioAuth>(
          SagaOperations.Autocadastro.ROLLBACK_AUTH,
          List.of(state.getSagaData().getUsuarioAuth()),
          state.getCorrelationId()),
          RabbitmqConsts.AUTH_SAGA_KEY);
    }

    if (state.getSagaData().getContaCriada() != null) {
      state.setStep(AutocadastroPasso.COMPENSANDO_CONTA);
      SagaProducer<AutocadastroDto.ContaCriada> producer = producerFactory.create();
      producer.enviarMenssagem(new SagaMessageWrapper<AutocadastroDto.ContaCriada>(
          SagaOperations.Autocadastro.ROLLBACK_CONTA,
          List.of(AutocadastroDto.ContaCriada.builder()
              .solicitacaoId(getSolicitacaoId(state))
              .conta(state.getSagaData().getContaCriada())
              .build()),
          state.getCorrelationId()),
          RabbitmqConsts.CONTAS_SAGA_KEY);
    }

    if (state.getSagaData().getSolicitacao() != null) {
      state.setStep(AutocadastroPasso.COMPENSANDO_SOLICITACAO);
      SagaProducer<AutocadastroDto.Solicitacao> producer = producerFactory.create();
      producer.enviarMenssagem(new SagaMessageWrapper<AutocadastroDto.Solicitacao>(
          SagaOperations.Autocadastro.ROLLBACK_SOLICITACAO,
          List.of(state.getSagaData().getSolicitacao()),
          state.getCorrelationId()),
          RabbitmqConsts.USERS_SAGA_KEY);
    }

    enviarEmailFalha(state);
    state.setStep(AutocadastroPasso.FINALIZADO);
    state.setStatus(SagaStatus.FAILED);
    sagas.remove(state.getCorrelationId());
  }

  private void enviarEmailFalha(SagaState<AutocadastroData> state) {
    var email = getClienteEmail(state);
    if (email == null) {
      return;
    }

    SagaProducer<AutocadastroDto.Notificacao> producer = producerFactory.create();
    producer.enviarMenssagem(new SagaMessageWrapper<AutocadastroDto.Notificacao>(
        SagaOperations.Autocadastro.ENVIAR_EMAIL_FALHA,
        List.of(criarNotificacaoFalha(state, email)),
        state.getCorrelationId()),
        RabbitmqConsts.MAIL_SAGA_KEY);
  }

  private AutocadastroDto.Notificacao criarNotificacaoAprovacao(SagaState<AutocadastroData> state) {
    var solicitacao = state.getSagaData().getSolicitacao();
    var cliente = solicitacao.getCliente();
    var conta = state.getSagaData().getContaCriada();
    var usuarioAuth = state.getSagaData().getUsuarioAuth();

    return AutocadastroDto.Notificacao.builder()
        .solicitacaoId(solicitacao.getId())
        .destinatario(cliente.getEmail())
        .assunto("Sua conta BANTADS foi aprovada")
        .conteudoHtml("""
            <h2>Sua conta BANTADS foi aprovada</h2>
            <p>Ola, %s.</p>
            <p>Sua solicitacao de autocadastro foi aprovada e sua conta ja foi criada.</p>
            <h3>Dados de acesso</h3>
            <ul>
              <li><strong>Numero da conta:</strong> %s</li>
              <li><strong>Usuario:</strong> %s</li>
              <li><strong>Senha temporaria:</strong> %s</li>
            </ul>
            <p>Altere sua senha no primeiro acesso.</p>
            """.formatted(
            escapeHtml(cliente.getNome()),
            escapeHtml(conta.getNumero()),
            escapeHtml(usuarioAuth.getEmail()),
            escapeHtml(usuarioAuth.getSenhaTemporaria())))
        .tipo(AutocadastroDto.TipoNotificacao.APROVACAO)
        .build();
  }

  private AutocadastroDto.Notificacao criarNotificacaoFalha(SagaState<AutocadastroData> state, String email) {
    var solicitacaoId = getSolicitacaoId(state);
    var nome = getClienteNome(state);

    return AutocadastroDto.Notificacao.builder()
        .solicitacaoId(solicitacaoId)
        .destinatario(email)
        .assunto("Sua solicitacao BANTADS nao foi concluida")
        .conteudoHtml("""
            <h2>Sua solicitacao de autocadastro nao foi concluida</h2>
            <p>Ola, %s.</p>
            <p>Houve uma falha interna durante o processamento da sua solicitacao.</p>
            <p>Nenhuma conta foi liberada para uso. Tente realizar o autocadastro novamente mais tarde.</p>
            """.formatted(escapeHtml(nome)))
        .tipo(AutocadastroDto.TipoNotificacao.FALHA)
        .build();
  }

  private Long getSolicitacaoId(SagaState<AutocadastroData> state) {
    return state.getSagaData().getSolicitacao() == null
        ? null
        : state.getSagaData().getSolicitacao().getId();
  }

  private String getClienteEmail(SagaState<AutocadastroData> state) {
    if (state.getSagaData().getSolicitacao() != null
        && state.getSagaData().getSolicitacao().getCliente() != null) {
      return state.getSagaData().getSolicitacao().getCliente().getEmail();
    }

    if (state.getSagaData().getEntrada() != null
        && state.getSagaData().getEntrada().getCliente() != null) {
      return state.getSagaData().getEntrada().getCliente().getEmail();
    }

    return null;
  }

  private String getClienteNome(SagaState<AutocadastroData> state) {
    if (state.getSagaData().getSolicitacao() != null
        && state.getSagaData().getSolicitacao().getCliente() != null) {
      return state.getSagaData().getSolicitacao().getCliente().getNome();
    }

    if (state.getSagaData().getEntrada() != null
        && state.getSagaData().getEntrada().getCliente() != null) {
      return state.getSagaData().getEntrada().getCliente().getNome();
    }

    return "cliente";
  }

  private String gerarSenhaTemporaria() {
    var senha = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      senha.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
    }

    return senha.toString();
  }

  private String escapeHtml(String value) {
    if (value == null) {
      return "";
    }

    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

}
