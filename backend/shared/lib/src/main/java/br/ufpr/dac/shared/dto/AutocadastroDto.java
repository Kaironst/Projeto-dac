package br.ufpr.dac.shared.dto;

import java.time.LocalDateTime;

import br.ufpr.dac.shared.dto.ContasDto.Conta;
import br.ufpr.dac.shared.dto.GerentesDto.Gerente;
import br.ufpr.dac.shared.dto.UsersDto.Cliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AutocadastroDto {

  public enum StatusSolicitacao {
    PENDENTE,
    APROVADO,
    REJEITADO,
    FALHA
  }

  public enum TipoNotificacao {
    SOLICITACAO_RECEBIDA,
    APROVACAO,
    REJEICAO,
    FALHA
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SolicitacaoEntrada {
    private Cliente cliente;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Solicitacao {
    private Long id;
    private Cliente cliente;
    private Gerente gerente;
    private StatusSolicitacao status;
    private String motivoRejeicao;
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataAnalise;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Aprovacao {
    private Long solicitacaoId;
    private String cpf;
    private Long gerenteId;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Rejeicao {
    private Long solicitacaoId;
    private Long gerenteId;
    private String motivo;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ContaCriada {
    private Long solicitacaoId;
    private Conta conta;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class UsuarioAuth {
    private Long solicitacaoId;
    private Long clienteId;
    private String email;
    private String senhaTemporaria;
    private String tipo;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Notificacao {
    private Long solicitacaoId;
    private String destinatario;
    private String assunto;
    private String conteudoHtml;
    private TipoNotificacao tipo;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Falha {
    private Long solicitacaoId;
    private String cpf;
    private String email;
    private String motivo;
    private String operacaoOrigem;
  }

}
