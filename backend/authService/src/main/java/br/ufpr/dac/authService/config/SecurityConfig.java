package br.ufpr.dac.authService.config;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import br.ufpr.dac.authService.security.ClienteUserDetailsService;
import br.ufpr.dac.authService.security.JwtAuthenticationFilter;
import br.ufpr.dac.authService.security.JwtService;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, ClienteUserDetailsService userDetailsService,
      JwtService jwtService, PasswordEncoder passwordEncoder) throws Exception {

    JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

  }

  @Bean
  public RSAPrivateKey privateKey() throws Exception {

    String privateKeyPem = System.getenv("JWT_PRIVATE_KEY");

    String privateKeyContent = privateKeyPem
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s", "");

    byte[] decoded = Base64.getDecoder().decode(privateKeyContent);

    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
  }

  @Bean
  public RSAPublicKey publicKey() throws Exception {

    String publicKeyPem = System.getenv("JWT_PUBLIC_KEY");

    String publicKeyContent = publicKeyPem
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replaceAll("\\s", "");

    byte[] decoded = Base64.getDecoder().decode(publicKeyContent);

    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    return (RSAPublicKey) keyFactory.generatePublic(keySpec);
  }

  @Bean
  public KeyPair keyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
    return new KeyPair(publicKey, privateKey);
  }
}
