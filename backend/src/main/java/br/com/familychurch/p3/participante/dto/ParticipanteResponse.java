package br.com.familychurch.p3.participante.dto;

import br.com.familychurch.p3.historico.dto.HistoricoItemResponse;
import br.com.familychurch.p3.participante.EstadoCivil;
import br.com.familychurch.p3.participante.Participante;
import br.com.familychurch.p3.participante.TempoNaFamily;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ParticipanteResponse(

		UUID id,
		String nome,
		String cpf,
		String celular,
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
		String desejaServir,
		String habilidades,
		Boolean participaGc,
		Set<String> quaisGcs,
		Boolean consentimentoLgpd,
		LocalDateTime consentimentoLgpdTimestamp,
		List<HistoricoItemResponse> historico

) {

	public static ParticipanteResponse from(Participante participante, List<HistoricoItemResponse> historico) {
		return new ParticipanteResponse(
				participante.getId(),
				participante.getNome(),
				participante.getCpf(),
				participante.getCelular(),
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
				participante.getDesejaServir(),
				participante.getHabilidades(),
				participante.getParticipaGc(),
				participante.getQuaisGcs(),
				participante.getConsentimentoLgpd(),
				participante.getConsentimentoLgpdTimestamp(),
				historico);
	}

}
