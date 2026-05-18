package br.ufpr.dac.contasService.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufpr.dac.contasService.entity.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {

  Conta findByCliente(Long idCliente);

  Conta findByGerente(Long idGerente);

  boolean existsByNumero(String numero);

  List<Conta> findAllByCliente(Long idCliente);

  List<Conta> findAllByGerente(Long idGerente);

}
