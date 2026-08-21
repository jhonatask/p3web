package br.com.familychurch.p3.auth;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class PessoaJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final NucleoGateway nucleoGateway;

	public PessoaJwtAuthenticationConverter(NucleoGateway nucleoGateway) {
		this.nucleoGateway = nucleoGateway;
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		String sub = jwt.getSubject();
		UUID pessoaId = nucleoGateway.pessoaDaConta(sub).orElse(null);
		PessoaPrincipal principal = new PessoaPrincipal(pessoaId, sub);
		return new PessoaAuthenticationToken(principal, jwt, resolverAuthorities(sub));
	}

	private List<GrantedAuthority> resolverAuthorities(String sub) {
		return nucleoGateway.papeisDaConta(sub).stream()
				.map(papel -> new SimpleGrantedAuthority("ROLE_" + papel.toUpperCase(Locale.ROOT)))
				.map(GrantedAuthority.class::cast)
				.toList();
	}

}
