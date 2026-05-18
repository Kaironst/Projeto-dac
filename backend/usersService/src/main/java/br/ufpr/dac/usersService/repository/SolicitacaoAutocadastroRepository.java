package br.ufpr.dac.usersService.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.shared.dto.AutocadastroDto.StatusSolicitacao;
import br.ufpr.dac.usersService.entity.SolicitacaoAutocadastro;

@Repository
public interface SolicitacaoAutocadastroRepository extends JpaRepository<SolicitacaoAutocadastro, Long> {

  boolean existsByCpfAndStatus(String cpf, StatusSolicitacao status);

  List<SolicitacaoAutocadastro> findAllByStatus(StatusSolicitacao status);

  Optional<SolicitacaoAutocadastro> findFirstByCpfAndStatus(String cpf, StatusSolicitacao status);

}
