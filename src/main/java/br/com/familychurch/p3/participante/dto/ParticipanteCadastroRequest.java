package br.com.familychurch.p3.participante.dto;

import br.com.familychurch.p3.common.validation.Cpf;
import br.com.familychurch.p3.participante.EstadoCivil;
import br.com.familychurch.p3.participante.TempoNaFamily;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

public record ParticipanteCadastroRequest(

		@NotBlank
		String nome,

		@Cpf
		String cpf,

		@NotNull
		@Past
		LocalDate dataNascimento,

		@NotNull
		EstadoCivil estadoCivil,

		boolean temFilhos,

		Integer quantidadeFilhos,

		@NotBlank
		String cep,

		String rua,

		String numero,

		String complemento,

		@NotBlank
		String bairro,

		@NotBlank
		String cidade,

		@NotBlank
		@Size(min = 2, max = 2)
		String uf,

		String pontoReferencia,

		@NotNull
		TempoNaFamily tempoNaFamily,

		boolean batizado,

		boolean serveMinisterio,

		String qualMinisterioServe,

		String instagram,

		String facebook,

		Set<String> cursosRealizados,

		String cursosOutros,

		Set<String> eventosParticipou,

		String eventosOutros,

		boolean participaGc,

		Set<String> quaisGcs,

		boolean aceiteLgpd

) {
}
