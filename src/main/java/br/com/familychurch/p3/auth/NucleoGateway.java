package br.com.familychurch.p3.auth;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NucleoGateway {

	Optional<UUID> pessoaDaConta(String sub);

	List<PapelComEscopo> papeisDaConta(String sub);

}
