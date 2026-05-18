package br.ufpr.dac.authService.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import br.ufpr.dac.authService.entity.UsuarioAuth;

@Repository
public interface UsuarioAuthRepository extends MongoRepository<UsuarioAuth, String> {

  Optional<UsuarioAuth> findByEmailIgnoreCase(String email);

  void deleteByEmailIgnoreCase(String email);

}
