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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.ufpr.dac.authService.security.ClienteUserDetailsService;

@Configuration
public class SecurityConfig {

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

  // valida o login, verifica se o usuario existe e se a senha bate quando os
  // dados são enviados
  @Bean
  public AuthenticationProvider authenticationProvider(ClienteUserDetailsService userDetailsService,
      PasswordEncoder encoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(encoder);
    return provider;
  }

  // retorna Authentications validas ou não dependendo da Authentication recebida
  // AuthenticationConfig é um bean fornecido pelo spring que encapsula todos os
  // authenticationProvidersRegistrados
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  // criptografa senhas antes de guardar no banco e compara senha criptografada
  // com a crua
  @Bean
  public PasswordEncoder passwordEncoder() {
    // sha256+salt customizado para atender aos requisitos
    return new Sha256SaltEncoder();
  }

}
