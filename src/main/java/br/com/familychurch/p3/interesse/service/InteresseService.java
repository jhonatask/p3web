package br.com.familychurch.p3.interesse.service;

import br.com.familychurch.p3.interesse.dto.InteresseRequest;
import br.com.familychurch.p3.interesse.dto.InteresseResponse;
import java.util.List;
import java.util.UUID;

public interface InteresseService {

	InteresseResponse registrar(UUID aulaId, InteresseRequest request);

	List<InteresseResponse> listarPorTurma(UUID turmaId);

}
