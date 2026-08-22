package br.com.familychurch.p3.auth;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NucleoGatewayJdbc implements NucleoGateway {

	private final JdbcTemplate jdbcTemplate;

	public NucleoGatewayJdbc(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Optional<UUID> pessoaDaConta(String sub) {
		UUID pessoaId = jdbcTemplate.queryForObject("select core.pessoa_da_conta(?::uuid)", UUID.class, sub);
		return Optional.ofNullable(pessoaId);
	}

	@Override
	public List<String> papeisDaConta(String sub) {
		return jdbcTemplate.queryForList("select papel from core.papeis_da_conta(?::uuid)", String.class, sub);
	}

}
