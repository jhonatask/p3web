package br.com.familychurch.p3.aula.service;

import br.com.familychurch.p3.aula.dto.AulaRequest;
import br.com.familychurch.p3.aula.dto.AulaResponse;
import java.util.List;
import java.util.UUID;

public interface AulaService {

	AulaResponse criar(UUID turmaId, AulaRequest request);

	List<AulaResponse> listarPorTurma(UUID turmaId);

	AulaResponse buscarPorId(UUID id);

	byte[] gerarImagemQrCode(UUID id);

}
