package br.com.familychurch.p3.participante.dto;

import br.com.familychurch.p3.participante.EstadoCivil;
import br.com.familychurch.p3.participante.Participante;
import br.com.familychurch.p3.participante.TempoNaFamily;
import java.time.LocalDate;
import java.util.Set;

/**
 * Dados do cadastro base para pre-preencher a tela de atualizacao - mesmos
 * campos de {@link ParticipanteCadastroRequest}, sem historico (a tela de
 * edicao nao mostra o historico completo, so os campos do formulario).
 */
public record ParticipanteEdicaoResponse(

		String nome,
		String cpf,
		String celular,
		String email,
		LocalDate dataNascimento,
		EstadoCivil estadoCivil,
		Boolean temFilhos,
		Integer quantidadeFilhos,
		EnderecoResponse endereco,
		TempoNaFamily tempoNaFamily,
		Boolean batizado,
		Boolean serveMinisterio,
		String qualMinisterioServe,
		String instagram,
		String facebook,
		Set<String> cursosRealizados,
		Set<String> eventosParticipou,
		Boolean participaGc,
		Set<String> quaisGcs

) {

	public static ParticipanteEdicaoResponse from(Participante participante, Set<String> cursosRealizados,
			Set<String> eventosParticipou) {
		return new ParticipanteEdicaoResponse(
				participante.getNome(),
				participante.getCpf(),
				participante.getCelular(),
				participante.getEmail(),
				participante.getDataNascimento(),
				participante.getEstadoCivil(),
				participante.getTemFilhos(),
				participante.getQuantidadeFilhos(),
				participante.getEndereco() != null ? EnderecoResponse.from(participante.getEndereco()) : null,
				participante.getTempoNaFamily(),
				participante.getBatizado(),
				participante.getServeMinisterio(),
				participante.getQualMinisterioServe(),
				participante.getInstagram(),
				participante.getFacebook(),
				cursosRealizados,
				eventosParticipou,
				participante.getParticipaGc(),
				participante.getQuaisGcs());
	}

}
