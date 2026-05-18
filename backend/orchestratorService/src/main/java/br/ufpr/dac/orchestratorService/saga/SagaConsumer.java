package br.ufpr.dac.orchestratorService.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.orchestratorService.saga.autocadastroSaga.AutocadastroOrchestration;
import br.ufpr.dac.orchestratorService.saga.gerentesSaga.insertGerentes.InsertGerentesOrchestration;
import br.ufpr.dac.shared.dto.AutocadastroDto;
import br.ufpr.dac.shared.dto.GerentesDto;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations.Autocadastro;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations.InsertGerente;
import lombok.AllArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class SagaConsumer {

  private final InsertGerentesOrchestration insertGerentesOrchestration;
  private final AutocadastroOrchestration autocadastroOrchestration;
  private final ObjectMapper mapper;

  @RabbitListener(queues = RabbitmqConsts.ORCHESTRATOR_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {
    try {

      switch (message.getOperation()) {

        // para a saga inserir gerente
        // ====================================================================
        case InsertGerente.START -> {
          insertGerentesOrchestration
              .StartSaga(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<GerentesDto.Gerente>>() {
                  }));
        }
        case InsertGerente.GET_COM_MAIS_CONTAS_RESULT, InsertGerente.GET_COM_MAIS_CONTAS_ERROR -> {
          insertGerentesOrchestration
              .handleGerenteFound(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
        }
        case InsertGerente.INSERIR_NOVO_RESULT, InsertGerente.INSERIR_NOVO_ERROR -> {
          insertGerentesOrchestration
              .handleGerenteInserted(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
        }
        case InsertGerente.MOVER_CONTAS_RESULT, InsertGerente.MOVER_CONTAS_ERROR -> {
          insertGerentesOrchestration.handleContasSwapped(message);
        }
        // ===================================================================
        // para a saga de autocadastro
        // ====================================================================
        case Autocadastro.START -> {
          autocadastroOrchestration
              .startSaga(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<AutocadastroDto.SolicitacaoEntrada>>() {
                  }));
        }
        case Autocadastro.APROVAR_SOLICITACAO -> {
          autocadastroOrchestration
              .startAprovacaoSaga(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<AutocadastroDto.Aprovacao>>() {
                  }));
        }
        case Autocadastro.VALIDAR_CPF_RESULT, Autocadastro.VALIDAR_CPF_ERROR -> {
          autocadastroOrchestration
              .handleCpfValidado(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<AutocadastroDto.SolicitacaoEntrada>>() {
                  }));
        }
        case Autocadastro.REGISTRAR_SOLICITACAO_RESULT, Autocadastro.REGISTRAR_SOLICITACAO_ERROR -> {
          autocadastroOrchestration
              .handleSolicitacaoRegistrada(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<AutocadastroDto.Solicitacao>>() {
                  }));
        }
        case Autocadastro.ESCOLHER_GERENTE_RESULT, Autocadastro.ESCOLHER_GERENTE_ERROR -> {
          autocadastroOrchestration
              .handleGerenteEscolhido(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<Long>>() {
                  }));
        }
        case Autocadastro.VINCULAR_GERENTE_RESULT, Autocadastro.VINCULAR_GERENTE_ERROR -> {
          autocadastroOrchestration
              .handleGerenteVinculado(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<AutocadastroDto.Solicitacao>>() {
                  }));
        }
        case Autocadastro.APROVAR_SOLICITACAO_RESULT, Autocadastro.APROVAR_SOLICITACAO_ERROR -> {
          autocadastroOrchestration
              .handleSolicitacaoAprovada(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<AutocadastroDto.Solicitacao>>() {
                  }));
        }
        case Autocadastro.CRIAR_CONTA_RESULT, Autocadastro.CRIAR_CONTA_ERROR -> {
          autocadastroOrchestration
              .handleContaCriada(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<AutocadastroDto.ContaCriada>>() {
                  }));
        }
        case Autocadastro.CRIAR_AUTH_RESULT, Autocadastro.CRIAR_AUTH_ERROR -> {
          autocadastroOrchestration
              .handleAuthCriado(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<AutocadastroDto.UsuarioAuth>>() {
                  }));
        }
        case Autocadastro.ENVIAR_EMAIL_APROVACAO_RESULT, Autocadastro.ENVIAR_EMAIL_APROVACAO_ERROR -> {
          autocadastroOrchestration
              .handleEmailAprovacaoEnviado(mapper.convertValue(
                  message,
                  new TypeReference<SagaMessageWrapper<AutocadastroDto.Notificacao>>() {
                  }));
        }
        // ===================================================================

      }

    } catch (Exception e) {
      System.out.println("error on sagaConsumer");
      e.printStackTrace();
    }
  }
}
