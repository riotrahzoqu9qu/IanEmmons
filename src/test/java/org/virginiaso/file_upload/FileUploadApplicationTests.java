package org.virginiaso.file_upload;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.virginiaso.file_upload.util.AwsDetector;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

@SpringBootTest
class FileUploadApplicationTests {
	@Test
	void contextLoads() {
	}

	@Test
	void timeZoneTest() {
		Instant utcNow = Instant.now();
		ZonedDateTime easternNow = ZonedDateTime.ofInstant(utcNow, ZoneId.of("America/New_York"));
		System.out.format("UTC now      = '%1$s'%n", DateTimeFormatter.ISO_INSTANT.format(utcNow));
		System.out.format("Eastern date = '%1$s'%n", DateTimeFormatter.ISO_LOCAL_DATE.format(easternNow));
		System.out.format("Eastern time = '%1$s'%n", DateTimeFormatter.ISO_LOCAL_TIME.format(easternNow));
	}

	@Test
	void awsDetectorTest() {
		System.out.format("AWS public host name: '%1$s'%n", AwsDetector.getAwsPublicHostname());
	}

	@Test
	void awsS3Test() {
		try (S3Client s3Client = S3Client.builder()
			.credentialsProvider(ProfileCredentialsProvider.create("iemmons-api"))
			.region(Region.US_EAST_1)
			.build()) {

			ListBucketsResponse lbResponse = s3Client.listBuckets();
			System.out.format("S3 Buckets (%1$d)%n", lbResponse.buckets().size());
			lbResponse.buckets().stream()
				.forEach(bucket -> System.out.format("   '%1$s'%n", bucket.name()));
		}
	}
}
