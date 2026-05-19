package br.ufpr.dac.authService.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.ufpr.dac.authService.entity.UsuarioAuth;
import br.ufpr.dac.authService.repository.UsuarioAuthRepository;
import br.ufpr.dac.shared.dto.AuthDto;
import br.ufpr.dac.shared.dto.AutocadastroDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UsuarioAuthRepository repo;
  private final BCryptPasswordEncoder passwordEncoder;

  @Value("${auth.jwt.secret}")
  private String jwtSecret;

  @Value("${auth.jwt.expiration-seconds}")
  private long jwtExpirationSeconds;

  public AutocadastroDto.UsuarioAuth criarUsuario(AutocadastroDto.UsuarioAuth usuarioAuth) {
    if (usuarioAuth.getEmail() == null || usuarioAuth.getSenhaTemporaria() == null) {
      throw new IllegalArgumentException("Email e senha temporaria sao obrigatorios para Auth.");
    }

    repo.findByEmailIgnoreCase(usuarioAuth.getEmail()).ifPresent(usuario -> {
      throw new IllegalStateException("Usuario Auth ja existe para o email informado.");
    });

    repo.save(UsuarioAuth.builder()
        .clienteId(usuarioAuth.getClienteId())
        .email(usuarioAuth.getEmail())
        .senhaHash(passwordEncoder.encode(usuarioAuth.getSenhaTemporaria()))
        .tipo(usuarioAuth.getTipo())
        .build());

    return usuarioAuth;
  }

  public void removerUsuario(AutocadastroDto.UsuarioAuth usuarioAuth) {
    if (usuarioAuth != null && usuarioAuth.getEmail() != null) {
      repo.deleteByEmailIgnoreCase(usuarioAuth.getEmail());
    }
  }

  public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
    if (request.getEmail() == null || request.getSenha() == null) {
      throw new IllegalArgumentException("Email e senha sao obrigatorios.");
    }

    var usuario = repo.findByEmailIgnoreCase(request.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("Credenciais invalidas."));
    if (!passwordEncoder.matches(request.getSenha(), usuario.getSenhaHash())) {
      throw new IllegalArgumentException("Credenciais invalidas.");
    }

    return AuthDto.LoginResponse.builder()
        .token(gerarJwt(usuario))
        .tokenType("Bearer")
        .tipo(usuario.getTipo())
        .email(usuario.getEmail())
        .clienteId(usuario.getClienteId())
        .build();
  }

  private String gerarJwt(UsuarioAuth usuario) {
    var now = Instant.now().getEpochSecond();
    var expiresAt = now + jwtExpirationSeconds;
    var header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    var payload = """
        {"sub":"%s","tipo":"%s","clienteId":%d,"iat":%d,"exp":%d}
        """.formatted(
        escapeJson(usuario.getEmail()),
        escapeJson(usuario.getTipo()),
        usuario.getClienteId() == null ? 0L : usuario.getClienteId(),
        now,
        expiresAt).trim();

    var encodedHeader = encodeBase64Url(header.getBytes(StandardCharsets.UTF_8));
    var encodedPayload = encodeBase64Url(payload.getBytes(StandardCharsets.UTF_8));
    var signature = assinar(encodedHeader + "." + encodedPayload);
    return encodedHeader + "." + encodedPayload + "." + signature;
  }

  private String assinar(String value) {
    try {
      var mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return encodeBase64Url(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("Nao foi possivel gerar token JWT.", e);
    }
  }

  private String encodeBase64Url(byte[] value) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
  }

  private String escapeJson(String value) {
    if (value == null) {
      return "";
    }

    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"");
  }

}
