package br.ufpr.dac.usersService.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.usersService.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

  Cliente findByEmailIgnoreCase(String email);

  List<Cliente> findAllByEmailIgnoreCase(List<String> emails);

  Cliente findByCpf(String cpf);

  List<Cliente> findAllByCpf(List<String> cpfs);

}
