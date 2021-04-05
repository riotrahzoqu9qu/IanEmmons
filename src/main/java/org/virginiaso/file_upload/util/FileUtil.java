package org.virginiaso.file_upload.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.lang3.tuple.Pair;

public final class FileUtil {
	private FileUtil() {}	// prevent instantiation

	public static File appendToFileStem(File file, String stemSuffix) {
		File dir = file.getParentFile();
		Pair<String, String> stemExt = getStemExtPair(file.getName());
		String newFileName = stemExt.getRight().isEmpty()
			? stemExt.getLeft() + stemSuffix
			: String.format("%1$s%2$s.%3$s", stemExt.getLeft(), stemSuffix, stemExt.getRight());
		return new File(dir, newFileName);
	}

	public static Pair<String, String> getStemExtPair(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex == -1) {
			return Pair.of(fileName, "");
		} else {
			String stem = fileName.substring(0, dotIndex);
			String ext = fileName.substring(dotIndex + 1);
			return Pair.of(stem, ext);
		}
	}

	public static File move(File source, File target) throws IOException {
		return Files.move(source.toPath(), target.toPath(),
			StandardCopyOption.ATOMIC_MOVE).toFile();
	}
}
