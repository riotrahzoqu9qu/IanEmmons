package org.virginiaso.file_upload.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.MissingResourceException;

import org.apache.commons.lang3.tuple.Pair;

public final class FileUtil {
	public static Charset CHARSET = StandardCharsets.UTF_8;

	private FileUtil() {}	// prevent instantiation

	public static File appendToFileStem(File file, String stemSuffix) {
		var dir = file.getParentFile();
		Pair<String, String> stemExt = getStemExtPair(file.getName());
		var newFileName = stemExt.getRight().isEmpty()
			? stemExt.getLeft() + stemSuffix
			: String.format("%1$s%2$s.%3$s", stemExt.getLeft(), stemSuffix, stemExt.getRight());
		return new File(dir, newFileName);
	}

	public static Pair<String, String> getStemExtPair(String fileName) {
		var dotIndex = fileName.lastIndexOf('.');
		if (dotIndex == -1) {
			return Pair.of(fileName, "");
		} else {
			var stem = fileName.substring(0, dotIndex);
			var ext = fileName.substring(dotIndex + 1);
			return Pair.of(stem, ext);
		}
	}

	public static File move(File source, File target) throws IOException {
		return Files
			.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
			.toFile();
	}

	public static InputStream getResourceAsInputStream(String rsrcName) {
		var cl = Thread.currentThread().getContextClassLoader();
		var result = cl.getResourceAsStream(rsrcName);
		if (result == null) {
			var msg = String.format("Resource not found: '%1$s'", rsrcName);
			throw new MissingResourceException(msg, null, rsrcName);
		}
		return result;
	}

	public static Reader getResourceAsReader(String rsrcName) {
		var is = getResourceAsInputStream(rsrcName);
		return new InputStreamReader(is, CHARSET);
	}
}
