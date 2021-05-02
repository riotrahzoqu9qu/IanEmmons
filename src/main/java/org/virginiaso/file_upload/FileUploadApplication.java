package org.virginiaso.file_upload;

import java.security.Security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileUploadApplication {
	public static void main(String[] args) {
		// Per AWS guidelines.  See:
		// https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-jvm-ttl.html
		Security.setProperty("networkaddress.cache.ttl", "30");

		SpringApplication.run(FileUploadApplication.class, args);
	}
}
