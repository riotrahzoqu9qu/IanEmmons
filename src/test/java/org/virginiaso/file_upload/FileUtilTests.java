package org.virginiaso.file_upload;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

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
