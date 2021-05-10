package org.virginiaso.file_upload.util;

public class ValidationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ValidationException(String messageFormat, Object... args) {
		super(String.format(messageFormat, args));
	}

	public ValidationException(Throwable cause, String messageFormat, Object... args) {
		super(String.format(messageFormat, args), cause);
	}

	public ValidationException(String message, Throwable cause,
		boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
