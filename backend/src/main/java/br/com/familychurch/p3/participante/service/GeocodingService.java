package br.com.familychurch.p3.participante.service;

import java.util.Optional;

public interface GeocodingService {

	Optional<Coordenadas> geocodificar(EnderecoParaGeocodificar endereco);

}
