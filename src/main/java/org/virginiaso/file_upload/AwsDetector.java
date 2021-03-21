package org.virginiaso.file_upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class AwsDetector {
	private static final String AWS_METADATA_URL = "http://169.254.169.254/latest/meta-data/public-hostname";
	private static final Logger LOG = LoggerFactory.getLogger(AwsDetector.class);

	private AwsDetector() {}	// prevent instantiation

	public static String getAwsPublicHostname() {
		long start = System.currentTimeMillis();
		String result = null;
		try {
			result = new RestTemplate().getForObject(AWS_METADATA_URL, String.class).trim();
			LOG.info("Running on AWS EC2 instance '{}'", result);
		} catch (RestClientException ex) {
			LOG.info("Running outside AWS");
		}
		long duration = System.currentTimeMillis() - start;
		LOG.info("AWS detection finished in {} milliseconds", duration);
		return result;
	}

	public static void main(String[] args) {
		try {
			System.out.format("AWS public host name: '%1$s'%n", getAwsPublicHostname());
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}
