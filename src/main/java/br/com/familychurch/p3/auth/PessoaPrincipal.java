package br.com.familychurch.p3.auth;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

	public Set<UUID> turmasEscopoLiderP3() {
		return papeis.stream()
				.filter(papel -> PAPEL_LIDER_P3.equalsIgnoreCase(papel.papel()))
				.map(PapelComEscopo::scopeId)
				.filter(Objects::nonNull)
				.map(PessoaPrincipal::tentarConverterUuid)
				.flatMap(Optional::stream)
				.collect(Collectors.toUnmodifiableSet());
	}

	private static Optional<UUID> tentarConverterUuid(String valor) {
		try {
			return Optional.of(UUID.fromString(valor));
		} catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

}
