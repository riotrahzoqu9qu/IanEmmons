package org.virginiaso.file_upload.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class S3UriTest {
	@ParameterizedTest
	@CsvSource({
		"s3://bucket/root-key/hb*-research.xml,bucket,root-key/hb*-research.xml"
			+ ",s3://bucket/root-key/hb*-research.xml",
		"S3://bucket/root-key/hb*-research.xml,bucket,root-key/hb*-research.xml"
			+ ",s3://bucket/root-key/hb*-research.xml",
	})
	void legalS3UriTest(String uriStr, String expectedBucket, String expectedKey,
			String expectedReconstructedUri) {
		S3Uri uri = new S3Uri(uriStr);
		assertEquals(expectedBucket, uri.getBucket());
		assertEquals(expectedKey, uri.getKey());
		assertEquals(expectedKey, uri.getKey());
		assertEquals(expectedReconstructedUri, uri.toString());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
		"  ",
		"\t",
		"\n",
		"http://example.org/",
		"https://bucket.s3-us-east-1.amazonaws.com/root-key/hb*-research.xml",
		"s3:bucket/key",
		"s3:/bucket/key",
		"s3://bucket",
		"s3://bucket/",
		"s3:///key",
		"s3://",
	})
	void illegalS3UriTest(String uriStr) {
		assertThrows(RuntimeException.class, () -> new S3Uri(uriStr));
	}
}
