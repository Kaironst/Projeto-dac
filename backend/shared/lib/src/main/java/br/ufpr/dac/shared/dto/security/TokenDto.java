package br.ufpr.dac.shared.dto.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenDto {

  //enriquecimento do token para os dados do professor será feito pelo apigateway como api composition
  private String token;
}
