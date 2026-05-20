package br.ufpr.dac.gerentesService.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.gerentesService.entity.Gerente;

@Repository
public interface GerenteRepository extends JpaRepository<Gerente, Long> {

  Gerente findByEmailIgnoreCase(String email);

  List<Gerente> findAllByEmailIgnoreCase(List<String> emails);

  Gerente findByCpf(String cpf);

  List<Gerente> findAllByCpf(List<String> cpfs);

}
