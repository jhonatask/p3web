package br.com.familychurch.p3.presenca.service;

import br.com.familychurch.p3.presenca.dto.CheckinRequest;
import br.com.familychurch.p3.presenca.dto.MarcarPresencaManualRequest;
import br.com.familychurch.p3.presenca.dto.PresencaResponse;
import java.util.List;
import java.util.UUID;

public interface PresencaService {

	PresencaResponse checkin(CheckinRequest request);

	PresencaResponse marcarManual(UUID aulaId, MarcarPresencaManualRequest request, UUID professorId);

	List<PresencaResponse> listarPorAula(UUID aulaId);

}
