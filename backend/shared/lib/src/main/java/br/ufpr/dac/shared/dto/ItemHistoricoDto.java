package br.ufpr.dac.shared.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

public class ItemHistoricoDto {

  @Data
  @EqualsAndHashCode(callSuper = false)
  @SuperBuilder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Message extends MessageWrapper {
    private String operation;
    private List<ItemHistorico> data;
  }

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

}
