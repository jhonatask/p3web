package br.com.familychurch.p3.participante.service;

import br.com.familychurch.p3.participante.dto.ParticipanteCadastroRequest;
import br.com.familychurch.p3.participante.dto.ParticipanteCadastroResponse;
import br.com.familychurch.p3.participante.dto.ParticipanteEdicaoResponse;
import br.com.familychurch.p3.participante.dto.ParticipanteResponse;
import br.com.familychurch.p3.participante.dto.ParticipanteResumoResponse;

public interface ParticipanteService {

	ParticipanteCadastroResponse cadastrar(ParticipanteCadastroRequest request);

	ParticipanteResponse buscarPorCpf(String cpf);

	ParticipanteResumoResponse buscarResumoPorCpf(String cpf);

	ParticipanteEdicaoResponse buscarParaEdicaoPorCpf(String cpf);

}
