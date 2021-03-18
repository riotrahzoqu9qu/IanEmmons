package org.virginiaso.file_upload;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class StreamUtil {
	private StreamUtil() {}	// prevent instantiation

	public static <T, U extends Iterable<T>> Stream<T> from(U iterable) {
		return StreamSupport.stream(iterable.spliterator(), false);
	}
}
