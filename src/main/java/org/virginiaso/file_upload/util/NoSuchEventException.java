package org.virginiaso.file_upload.util;

public final class NoSuchEventException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoSuchEventException() {
	}

	public NoSuchEventException(String messageFormat, Object... args) {
		super(String.format(messageFormat, args));
	}

	public NoSuchEventException(Throwable cause) {
		super(cause);
	}

	public NoSuchEventException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchEventException(String message, Throwable cause,
		boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
