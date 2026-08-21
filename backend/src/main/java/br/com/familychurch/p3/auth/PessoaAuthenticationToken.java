package br.com.familychurch.p3.auth;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class PessoaAuthenticationToken extends AbstractAuthenticationToken {

	private final PessoaPrincipal principal;
	private final Jwt jwt;

	public PessoaAuthenticationToken(PessoaPrincipal principal, Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.principal = principal;
		this.jwt = jwt;
		setAuthenticated(true);
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}

	@Override
	public Object getCredentials() {
		return jwt;
	}

}
