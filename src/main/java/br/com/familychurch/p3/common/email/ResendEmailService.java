package br.com.familychurch.p3.common.email;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailService implements EmailService {

	private static final URI RESEND_URI = URI.create("https://api.resend.com/emails");

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final String apiKey;
	private final String remetente;

	public ResendEmailService(
			@Value("${app.email.resend-api-key}") String apiKey,
			@Value("${app.email.remetente}") String remetente) {
		this.apiKey = apiKey;
		this.remetente = remetente;
	}

	@Override
	public void enviar(String destinatario, String assunto, String corpoHtml) {
		try {
			String corpo = """
					{"from":"%s","to":["%s"],"subject":"%s","html":"%s"}"""
					.formatted(escapar(remetente), escapar(destinatario), escapar(assunto), escapar(corpoHtml));
			HttpRequest requisicao = HttpRequest.newBuilder(RESEND_URI)
					.header("Authorization", "Bearer " + apiKey)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(corpo))
					.build();
			HttpResponse<String> resposta = httpClient.send(requisicao, HttpResponse.BodyHandlers.ofString());
			if (resposta.statusCode() >= 400) {
				throw new IllegalStateException(
						"Resend respondeu %d: %s".formatted(resposta.statusCode(), resposta.body()));
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Falha ao enviar e-mail via Resend", exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Falha ao enviar e-mail via Resend", exception);
		}
	}

	private String escapar(String valor) {
		return valor
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "");
	}

}
