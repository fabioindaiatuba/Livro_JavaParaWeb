package br.com.javaparaweb.financeiro.util;

public class RNException extends Exception {

	private static final long serialVersionUID = 1L;

	public RNException() {
	}

	public RNException(String message) {
		super(message);
	}

	public RNException(Throwable cause) {
		super(cause);
	}

	public RNException(String message, Throwable cause) {
		super(message, cause);
	}

	public RNException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
