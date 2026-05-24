package br.ufpr.dac.authService.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;

public class Sha256SaltEncoder implements PasswordEncoder {

  private final SecureRandom random = new SecureRandom();

  @Override
  public String encode(CharSequence rawPassword) {
    // gera salt aleatório (16 bytes aleatorizados)
    byte[] salt = new byte[16];
    random.nextBytes(salt);

    // realiza o hash
    try {
      // MessageDigest é uma classe do java que realiza o hashing de tamanho fixo
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      // atualiza o hash padrão com o salt aleatório
      digest.update(salt);
      // realiza o hashing com os chars virando bytes utf8
      byte[] hash = digest.digest(rawPassword.toString().getBytes(StandardCharsets.UTF_8));

      // bytes são trasformados em strings de representação em base 64, salt e hash
      // separados com :
      return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);

    } catch (Exception e) {
      throw new IllegalStateException();
    }

  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {

    try {

      // separa a senha codificada em salt e o hash em si
      String[] partesHashed = encodedPassword.split(":");
      if (partesHashed.length != 2)
        return false;
      byte[] salt = Base64.getDecoder().decode(partesHashed[0]);
      byte[] hash = Base64.getDecoder().decode(partesHashed[1]);

      // cria o digest originalmente usado para dar o hash na senha pela primeira vez
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(salt);

      // realiza o hashing da senha crua e compara os 2 hashes, retornando o valor
      byte[] hashNovo = digest.digest(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
      return Arrays.equals(hash, hashNovo);

    } catch (Exception e) {
      return false;
    }

  }

}
