package br.ufpr.dac.authService.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.ufpr.dac.authService.document.Conta;

public interface ContaRepository extends MongoRepository<Conta, String> {

  Conta findByEmailIgnoreCase(String email);

}
