package br.ufpr.dac.usersService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.usersService.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

  Cliente findByEmailIgnoreCase(String email);

  Cliente findByCpf(String cpf);

}
