package br.com.familychurch.p3.avaliacao.service;

import br.com.familychurch.p3.avaliacao.dto.AvaliacaoRequest;
import br.com.familychurch.p3.avaliacao.dto.AvaliacaoResponse;
import java.util.List;
import java.util.UUID;

public interface AvaliacaoService {

	AvaliacaoResponse registrar(UUID aulaId, AvaliacaoRequest request);

	List<AvaliacaoResponse> listarPorAula(UUID aulaId);

}
