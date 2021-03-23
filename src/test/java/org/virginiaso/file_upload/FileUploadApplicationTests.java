package org.virginiaso.file_upload;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;

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
		AmazonS3 s3client = AmazonS3ClientBuilder.standard()
			.withRegion(Regions.US_EAST_1)
			.withCredentials(new ProfileCredentialsProvider("iemmons-api"))
			.build();
		List<Bucket> buckets = s3client.listBuckets();
		System.out.format("S3 Buckets (%1$d)%n", buckets.size());
		buckets.stream()
			.forEach(bucket -> System.out.format("   '%1$s'%n", bucket.getName()));
		s3client.shutdown();
	}
}
