package org.virginiaso.file_upload.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "DMI_HARDCODED_ABSOLUTE_FILENAME",
	justification = "Hardcoded file names are okay in tests of file path utilities")
public class FileUtilTests {
	@Test
	void testWithFileExt() {
		File originalFile = new File("/foo/bar/baz.txt");
		File fileWithSuffix = FileUtil.appendToFileStem(originalFile, "-temp");
		assertEquals("/foo/bar/baz-temp.txt", fileWithSuffix.getPath());
	}

	@Test
	void testWithNoFileExt() {
		File originalFile = new File("/foo.bar/baz");
		File fileWithSuffix = FileUtil.appendToFileStem(originalFile, "-temp");
		assertEquals("/foo.bar/baz-temp", fileWithSuffix.getPath());
	}
}
