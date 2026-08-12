package br.com.familychurch.p3.participante.service;

public record EnderecoParaGeocodificar(

		String cep,
		String rua,
		String numero,
		String bairro,
		String cidade,
		String uf

) {
}
