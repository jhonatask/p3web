package br.com.familychurch.p3.presenca.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;

public class QrCodeInvalidoException extends NotFoundException {

	public QrCodeInvalidoException() {
		super("QR code invalido ou correspondente a nenhuma aula");
	}

}
