package br.ufpr.dac.authService.security;

import java.util.ArrayList;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.ufpr.dac.authService.repository.ContaRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ClienteUserDetailsService implements UserDetailsService {

  private final ContaRepository repo;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    try {
      var user = repo.findByEmailIgnoreCase(email);
      var roles = new ArrayList<SimpleGrantedAuthority>();
      user.getRoles().forEach(roleString -> {
        roles.add(new SimpleGrantedAuthority(roleString));
      });
      return new User(
          user.getEmail(),
          user.getSenha(),
          roles);

    } catch (BadCredentialsException e) {
      System.out.println("usuario não encontrado");
      throw new UsernameNotFoundException("usuario não encontrado", e);
    }

  }

}
