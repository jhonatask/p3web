package br.com.familychurch.p3.participante.service;

import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Implementacao provisoria sem chave do Google Geocoding disponivel: o cadastro
 * salva normalmente, apenas sem latitude/longitude. Trocar por uma implementacao
 * real (HTTP via RestClient) quando a chave existir, sem alterar ParticipanteServiceImpl.
 */
@Service
public class GeocodingServiceStub implements GeocodingService {

	@Override
	public Optional<Coordenadas> geocodificar(EnderecoParaGeocodificar endereco) {
		return Optional.empty();
	}

}
