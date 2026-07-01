package br.com.familychurch.p3.aluno.exception;

public class ConsentimentoLgpdObrigatorioException extends RuntimeException {

	public ConsentimentoLgpdObrigatorioException() {
		super("E necessario aceitar o consentimento de uso de dados (LGPD) para concluir o cadastro");
	}

}
