package br.com.familychurch.p3.aluno.exception;

import br.com.familychurch.p3.common.exception.ValidationException;

public class ConsentimentoLgpdObrigatorioException extends ValidationException {

	public ConsentimentoLgpdObrigatorioException() {
		super("E necessario aceitar o consentimento de uso de dados (LGPD) para concluir o cadastro");
	}

}
