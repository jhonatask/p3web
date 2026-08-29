package br.com.familychurch.p3.common.token;

import java.security.SecureRandom;

public final class GeradorToken {

	private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private GeradorToken() {
	}

	public static String gerar(int tamanho) {
		StringBuilder token = new StringBuilder(tamanho);
		for (int i = 0; i < tamanho; i++) {
			token.append(ALFABETO.charAt(SECURE_RANDOM.nextInt(ALFABETO.length())));
		}
		return token.toString();
	}

}
