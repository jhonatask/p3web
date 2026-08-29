package br.com.familychurch.p3.common.email;

public interface EmailService {

	void enviar(String destinatario, String assunto, String corpoHtml);

}
