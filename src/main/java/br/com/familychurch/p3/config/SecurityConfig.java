package br.com.familychurch.p3.config;

import br.com.familychurch.p3.auth.PessoaJwtAuthenticationConverter;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties.class)
public class SecurityConfig {

	private final CorsProperties corsProperties;
	private final PessoaJwtAuthenticationConverter pessoaJwtAuthenticationConverter;

	public SecurityConfig(CorsProperties corsProperties, PessoaJwtAuthenticationConverter pessoaJwtAuthenticationConverter) {
		this.corsProperties = corsProperties;
		this.pessoaJwtAuthenticationConverter = pessoaJwtAuthenticationConverter;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		// A area logada do lider (painel/gestao de turma) passa a chamar
		// endpoints autenticados via browser - o CORS nao pode mais ficar
		// restrito a /api/publico/**. allowCredentials habilita o header
		// Authorization (Bearer) em requisicoes cross-origin.
		CorsConfiguration configuracaoGeral = new CorsConfiguration();
		configuracaoGeral.setAllowedOrigins(corsProperties.allowedOrigins());
		configuracaoGeral.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
		configuracaoGeral.setAllowedHeaders(List.of("*"));
		configuracaoGeral.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuracaoGeral);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/publico/**").permitAll()
						.requestMatchers("/ws/**").permitAll()
						.requestMatchers("/api/**").authenticated()
						.anyRequest().permitAll())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(pessoaJwtAuthenticationConverter)))
				.build();
	}

}
