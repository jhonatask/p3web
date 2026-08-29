package br.com.familychurch.p3.auth;

import java.util.List;
import java.util.UUID;

public record PessoaPrincipal(UUID pessoaId, String sub, List<PapelComEscopo> papeis) {

	private static final String PAPEL_LIDER_P3 = "lider_p3";

	public boolean liderRestrito() {
		List<PapelComEscopo> gruposLider = papeis.stream()
				.filter(papel -> PAPEL_LIDER_P3.equalsIgnoreCase(papel.papel()))
				.toList();
		if (gruposLider.isEmpty()) {
			return false;
		}
		return gruposLider.stream().noneMatch(papel -> papel.scopeId() == null);
	}

}
