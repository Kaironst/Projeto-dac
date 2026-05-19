package br.ufpr.dac.authService.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import br.ufpr.dac.authService.entity.UsuarioAuth;
import br.ufpr.dac.authService.repository.UsuarioAuthRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthDataInitializer implements ApplicationRunner {

  private static final String DEFAULT_PASSWORD = "tads";

  private final UsuarioAuthRepository repo;
  private final BCryptPasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) {
    seed("cli1@bantads.com.br", "CLIENTE", 1L);
    seed("cli2@bantads.com.br", "CLIENTE", 2L);
    seed("cli3@bantads.com.br", "CLIENTE", 3L);
    seed("cli4@bantads.com.br", "CLIENTE", 4L);
    seed("cli5@bantads.com.br", "CLIENTE", 5L);

    seed("ger1@bantads.com.br", "GERENTE", null);
    seed("ger2@bantads.com.br", "GERENTE", null);
    seed("ger3@bantads.com.br", "GERENTE", null);
    seed("adm1@bantads.com.br", "ADMINISTRADOR", null);
  }

  private void seed(String email, String tipo, Long clienteId) {
    if (repo.findByEmailIgnoreCase(email).isPresent()) {
      return;
    }

    repo.save(UsuarioAuth.builder()
        .clienteId(clienteId)
        .email(email)
        .senhaHash(passwordEncoder.encode(DEFAULT_PASSWORD))
        .tipo(tipo)
        .build());
  }

}
