package br.com.familychurch.p3.auth;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioAdminDetails implements UserDetails {

	private final UsuarioAdmin usuarioAdmin;

	public UsuarioAdminDetails(UsuarioAdmin usuarioAdmin) {
		this.usuarioAdmin = usuarioAdmin;
	}

	public UsuarioAdmin getUsuarioAdmin() {
		return usuarioAdmin;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + usuarioAdmin.getPapel().name()));
	}

	@Override
	public String getPassword() {
		return usuarioAdmin.getSenha();
	}

	@Override
	public String getUsername() {
		return usuarioAdmin.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
