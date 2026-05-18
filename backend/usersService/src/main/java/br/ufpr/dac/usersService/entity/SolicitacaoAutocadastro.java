package br.ufpr.dac.usersService.entity;

import java.time.LocalDateTime;
import java.util.List;

import br.ufpr.dac.shared.dto.AutocadastroDto.StatusSolicitacao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class SolicitacaoAutocadastro {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nome;
  private String email;
  @Column(nullable = false)
  private String cpf;
  private Integer estado;
  private String telefone;
  private Double salario;
  private Long cliente;
  private Long gerente;

  @Enumerated(EnumType.STRING)
  private StatusSolicitacao status;

  private String motivoRejeicao;
  private LocalDateTime dataSolicitacao;
  private LocalDateTime dataAnalise;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EnderecoSolicitacao> enderecos;

}
