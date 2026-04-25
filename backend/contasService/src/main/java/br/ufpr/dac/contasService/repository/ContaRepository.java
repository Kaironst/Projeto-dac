package br.ufpr.dac.contasService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufpr.dac.contasService.entity.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {

  Conta findByCliente(Long idCliente);

  Conta findByGerente(Long idGerente);

}
