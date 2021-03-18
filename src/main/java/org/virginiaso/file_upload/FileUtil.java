package org.virginiaso.file_upload;

import java.io.File;

class FileUtil {
	private FileUtil() {}	// prevent instantiation

	public static File appendToFileStem(File file, String stemSuffix) {
		File dir = file.getParentFile();
		String name = file.getName();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) {
			return new File(dir, name + stemSuffix);
		} else {
			String stem = name.substring(0, dotIndex);
			String ext = name.substring(dotIndex + 1);
			return new File(dir, "%1$s%2$s.%3$s".formatted(stem, stemSuffix, ext));
		}
	}
}
