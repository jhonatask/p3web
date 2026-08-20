package br.com.familychurch.p3.aula.dto;

import br.com.familychurch.p3.aula.ModuloAula;
import br.com.familychurch.p3.aula.TipoAula;
import java.util.UUID;

public record TipoAulaResponse(

		UUID id,
		ModuloAula modulo,
		Integer numero,
		String tema,
		Integer ordem

) {

	public static TipoAulaResponse from(TipoAula tipoAula) {
		return new TipoAulaResponse(
				tipoAula.getId(), tipoAula.getModulo(), tipoAula.getNumero(), tipoAula.getTema(), tipoAula.getOrdem());
	}

}
