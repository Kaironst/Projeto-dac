package br.ufpr.dac.gerentesService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.gerentesService.entity.Gerente;

@Repository
public interface GerenteRepository extends JpaRepository<Gerente, Long> {

  Gerente findByEmailIgnoreCase(String email);

  Gerente findByCpf(String cpf);

}
