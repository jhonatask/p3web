package br.com.familychurch.p3.auth;

import java.util.Optional;
import java.util.UUID;

/**
 * Unica porta pro schema core: p3_app so tem EXECUTE nessas funcoes, sem
 * select direto em core.*.
 */
public interface NucleoGateway {

	/**
	 * @param sub auth_user_id do JWT ja validado - nunca vindo de parametro de request.
	 */
	Optional<UUID> pessoaDaConta(String sub);

}
