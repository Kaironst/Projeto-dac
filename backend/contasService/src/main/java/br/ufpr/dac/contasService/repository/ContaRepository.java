package br.ufpr.dac.contasService.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufpr.dac.contasService.entity.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {

  Conta findByCliente(Long idCliente);
  
  java.util.Optional<Conta> findByNumero(String numero);

  Conta findByGerente(Long idGerente);

  List<Conta> findAllByCliente(Long idCliente);

  List<Conta> findAllByGerente(Long idGerente);

}
