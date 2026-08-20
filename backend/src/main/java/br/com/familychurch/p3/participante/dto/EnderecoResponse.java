package br.com.familychurch.p3.participante.dto;

import br.com.familychurch.p3.participante.Endereco;

public record EnderecoResponse(

		String cep,
		String rua,
		String numero,
		String complemento,
		String bairro,
		String cidade,
		String uf,
		String pontoReferencia,
		Double latitude,
		Double longitude

) {

	public static EnderecoResponse from(Endereco endereco) {
		return new EnderecoResponse(
				endereco.getCep(),
				endereco.getRua(),
				endereco.getNumero(),
				endereco.getComplemento(),
				endereco.getBairro(),
				endereco.getCidade(),
				endereco.getUf(),
				endereco.getPontoReferencia(),
				endereco.getLatitude(),
				endereco.getLongitude());
	}

}
