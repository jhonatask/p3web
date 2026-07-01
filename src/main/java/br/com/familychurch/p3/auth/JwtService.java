package br.com.familychurch.p3.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

	private final JwtProperties jwtProperties;
	private final SecretKey chaveAssinatura;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.chaveAssinatura = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String gerarToken(UsuarioAdmin usuarioAdmin) {
		Instant agora = Instant.now();
		Instant expiracao = agora.plusSeconds(jwtProperties.expiracaoMinutos() * 60);

		return Jwts.builder()
				.subject(usuarioAdmin.getEmail())
				.claim("papel", usuarioAdmin.getPapel().name())
				.claim("nome", usuarioAdmin.getNome())
				.issuedAt(Date.from(agora))
				.expiration(Date.from(expiracao))
				.signWith(chaveAssinatura)
				.compact();
	}

	public String extrairEmail(String token) {
		return extrairClaims(token).getSubject();
	}

	public boolean tokenValido(String token) {
		try {
			extrairClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException exception) {
			return false;
		}
	}

	private Claims extrairClaims(String token) {
		return Jwts.parser()
				.verifyWith(chaveAssinatura)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

}
