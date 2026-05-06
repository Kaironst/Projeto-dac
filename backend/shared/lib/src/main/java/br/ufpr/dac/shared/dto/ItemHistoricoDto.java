package br.ufpr.dac.shared.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ItemHistoricoDto {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ItemHistorico {
    private Long id;
    private ContasDto.Conta contaOrigem;
    private ContasDto.Conta contaDestino;
    private LocalDateTime dataHora;
    private Integer tipo;
    private Double valorMovimentacao;

  }

  public static class TipoTransacao {
    public static final int DEPOSITO = 0;
    public static final int SAQUE = 1;
    public static final int TRANSACAO = 2;
  }

}
