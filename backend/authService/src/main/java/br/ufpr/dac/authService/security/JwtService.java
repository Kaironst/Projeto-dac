package br.ufpr.dac.authService.security;

import java.security.KeyPair;
import java.util.Date;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JwtService {

  private final KeyPair keyPair;

  public String generateToken(UserDetails details) {

    String username = details.getUsername();

    // coleção que contém as permissões do usuário a entrar no token
    List<String> roles = details.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    Date issuedAt = new Date();
    Date expiration = new Date(issuedAt.getTime() + 1000 * 60 * 60);

    return Jwts.builder()
        .subject(username)
        .claim("roles", roles)
        .issuedAt(issuedAt)
        .expiration(expiration)
        .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
        .compact();
  }

  // provavelmente vou mander o validateToken para o shared/
  public boolean validateToken(String token, UserDetails userDetails) {

    // verifica o token usando a chave e padga os claims dele
    Jws<Claims> jws = Jwts.parser()
        .verifyWith(keyPair.getPublic())
        .build()
        .parseSignedClaims(token);

    // pega o usuario e data de expiração do payload
    Claims payload = jws.getPayload();
    String username = payload.getSubject();
    Date expiration = payload.getExpiration();

    return username.equals(userDetails.getUsername()) && expiration.after(new Date());

  }

  public String extractUsername(String token) {
    return Jwts.parser()
        .verifyWith(keyPair.getPublic())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  public List<String> extractRoles(String token) {
    List<?> roles = Jwts.parser()
        .verifyWith(keyPair.getPublic())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("roles", List.class);

    return roles.stream()
        .map((Object o) -> o.toString())
        .toList();
  }

}
