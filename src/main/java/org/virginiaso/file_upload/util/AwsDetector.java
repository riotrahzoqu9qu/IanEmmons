package org.virginiaso.file_upload.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public final class AwsDetector {
	private static final String AWS_METADATA_URL = "http://169.254.169.254/latest/meta-data/public-hostname";
	private static final Logger LOG = LoggerFactory.getLogger(AwsDetector.class);

	private AwsDetector() {}	// prevent instantiation

	public static String getAwsPublicHostname() {
		long start = System.currentTimeMillis();
		String result = null;
		try {
			String os = System.getProperty("os.name");
			if (os != null && os.toLowerCase().contains("linux")) {
				result = StringUtil.safeTrim(
					new RestTemplate().getForObject(AWS_METADATA_URL, String.class));
				LOG.info("Running on AWS EC2 instance '{}'", result);
			}
		} catch (RestClientException ex) {
			// Do nothing
		}
		if (result == null) {
			LOG.info("Running outside AWS");
		}
		long duration = System.currentTimeMillis() - start;
		LOG.info("AWS detection finished in {} milliseconds", duration);
		return result;
	}
}
