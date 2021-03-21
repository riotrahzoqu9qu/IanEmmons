package org.virginiaso.file_upload;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
}
