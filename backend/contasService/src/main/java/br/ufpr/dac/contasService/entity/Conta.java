package br.ufpr.dac.contasService.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Conta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)

  // id também é o número da conta
  private Long id;

  // deve ser o id do cliente em UsersService
  private Long cliente;
  // deve ser o id do cliente em GerentesService
  private Long gerente;
  private Double saldo;
  private Double limite;
  private LocalDate dataCriacao;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "contaOrigem", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ItemHistorico> historicoOrigem;
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "contaDestino", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ItemHistorico> historicoDestino;

}
