package org.virginiaso.file_upload.util;

public class FieldValidationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public FieldValidationException() {
	}

	public FieldValidationException(String messageFormat, Object... args) {
		super(String.format(messageFormat, args));
	}

	public FieldValidationException(Throwable cause) {
		super(cause);
	}

	public FieldValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public FieldValidationException(String message, Throwable cause,
		boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
