package br.ufpr.dac.shared.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message wrapper genérico torna mais fácil adicionar propriedades
 * às menssagens
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageWrapper<T> {
  private String operation;
  private List<T> data;
}
