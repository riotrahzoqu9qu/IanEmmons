package org.virginiaso.file_upload.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.CompareToBuilder;

/** Immutable, structured representation for URIs of the form "s3://bucket/key". */
public final class S3Uri implements Comparable<S3Uri> {
	private static final Pattern S3_REGEX = Pattern.compile(
		"^[sS]3://(?<bucket>[^/]+)/(?<key>.+)$");

	private final String bucket;
	private final String key;

	public S3Uri(String bucket, String key) {
		this.bucket = Objects.requireNonNull(bucket, "bucket");
		this.key = Objects.requireNonNull(key, "key");
	}

	public S3Uri(String uri) {
		Matcher m = S3_REGEX.matcher(Objects.requireNonNull(uri, "uri"));
		if (!m.matches()) {
			throw new IllegalArgumentException(String.format(
				"Illegal S3 URI: '%1$s'", uri));
		}

		this.bucket = m.group("bucket");
		this.key = m.group("key");
	}

	public String getBucket() {
		return bucket;
	}

	public String getKey() {
		return key;
	}

	@Override
	public int compareTo(S3Uri rhs) {
		if (this == rhs) {
			return 0;
		}
		return new CompareToBuilder()
			.append(bucket, rhs.bucket)
			.append(key, rhs.key)
			.build();
	}

	@Override
	public boolean equals(Object rhs) {
		if (this == rhs) {
			return true;
		}
		if (!(rhs instanceof S3Uri)) {
			return false;
		}
		return compareTo((S3Uri) rhs) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bucket, key);
	}

	@Override
	public String toString() {
		return String.format("s3://%1$s/%2$s", bucket, key);
	}
}
