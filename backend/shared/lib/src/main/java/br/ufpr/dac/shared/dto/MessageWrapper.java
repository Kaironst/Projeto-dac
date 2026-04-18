package br.ufpr.dac.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Message wrapper genérico torna mais fácil adicionar propriedades
 * às menssagens
 *
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MessageWrapper {
  private String operation;
}
