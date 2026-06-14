package br.ufpr.dac.contasService.messaging.saga;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.ufpr.dac.contasService.messaging.saga.atualizarLimite.AtualizarLimiteAtualizarLimite;
import br.ufpr.dac.contasService.messaging.saga.autocadastro.AutocadastroCriarConta;
import br.ufpr.dac.contasService.messaging.saga.autocadastro.AutocadastroGetIdGerenteComMenosContasHandler;
import br.ufpr.dac.contasService.messaging.saga.insertGerente.GetIdGerenteComMaisContasHandler;
import br.ufpr.dac.contasService.messaging.saga.removerGerente.MoverContasHandler;
import br.ufpr.dac.contasService.messaging.saga.removerGerente.RemoverGerenteGetIdGerenteComMenosContasHandler;
import br.ufpr.dac.shared.dto.saga.SagaMessageWrapper;
import br.ufpr.dac.shared.keys.RabbitmqConsts;
import br.ufpr.dac.shared.keys.MessageOperations.SagaOperations;
import lombok.AllArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@AllArgsConstructor
@Component
public class SagaConsumer {

  private final GetIdGerenteComMaisContasHandler getIdGerenteComMaisContasHandler;
  private final RemoverGerenteGetIdGerenteComMenosContasHandler removerGerenteGetIdGerenteComMenosContasHandler;
  private final AutocadastroGetIdGerenteComMenosContasHandler autocadastroGetIdGerenteComMenosContasHandler;
  private final AtualizarLimiteAtualizarLimite atualizarLimiteAtualizarLimite;
  private final MoverContasHandler moverContasHandler;
  private final AutocadastroCriarConta autocadastroCriarConta;
  private final ObjectMapper mapper;

  @RabbitListener(queues = RabbitmqConsts.CONTAS_SAGA_QUEUE)
  public void recieveMessage(SagaMessageWrapper<Object> message) {

    switch (message.getOperation()) {
      case SagaOperations.InsertGerente.GET_COM_MAIS_CONTAS -> {
        getIdGerenteComMaisContasHandler.HandleGetIdGerenteComMaisContas(
            mapper.convertValue(message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
      case SagaOperations.InsertGerente.MOVER_CONTAS -> {
        moverContasHandler.handleMoverContasInsert(
            mapper.convertValue(message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
      case SagaOperations.RemoveGerente.GET_COM_MENOS_CONTAS -> {
        removerGerenteGetIdGerenteComMenosContasHandler.HandleGetIdGerenteComMenosContas(
            mapper.convertValue(message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
      case SagaOperations.RemoveGerente.MOVER_CONTAS -> {
        moverContasHandler.handleMoverContasRemove(
            mapper.convertValue(message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
      case SagaOperations.RemoveGerente.ROLLBACK_REVERTER_MOVER_CONTAS -> {
        moverContasHandler.handleRollbackMoverContasRemove(
            mapper.convertValue(message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
      case SagaOperations.Autocadastro.GET_GERENTE_MENOS_CONTAS -> {
        autocadastroGetIdGerenteComMenosContasHandler.handleGetIdGerenteComMenosContas(
            mapper.convertValue(message,
                new TypeReference<SagaMessageWrapper<Long>>() {
                }));
      }
      case SagaOperations.Autocadastro.CRIAR_CONTA -> {
        autocadastroCriarConta.handleCriarConta(
            mapper.convertValue(message,
                new TypeReference<SagaMessageWrapper<Double>>() {
                }));
      }
      case SagaOperations.AtualizarLimite.ATUALIZAR_CONTA -> {
        atualizarLimiteAtualizarLimite.handleAtualizarLimite(
            mapper.convertValue(message,
                new TypeReference<SagaMessageWrapper<Double>>() {
                }));
      }
    }
  }

}
