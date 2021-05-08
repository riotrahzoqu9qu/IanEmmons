package org.virginiaso.file_upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.virginiaso.file_upload.util.FileUtil;
import org.virginiaso.file_upload.util.S3Uri;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service("s3StorageService")
public class S3StorageServiceImpl implements StorageService {
	private static final Logger LOG = LoggerFactory.getLogger(S3StorageServiceImpl.class);

	private final S3Uri submissionRoot;
	private S3Client s3Client;

	public S3StorageServiceImpl(
		@Value("${fileUpload.aws-s3.submissionRoot}") String submissionRootUri) {
		submissionRoot = new S3Uri(submissionRootUri);
	}

	@PostConstruct
	public void initialize() {
		s3Client = S3Client.builder()
			.credentialsProvider(DefaultCredentialsProvider.create())
			.region(DefaultAwsRegionProviderChain.builder().build().getRegion())
			.build();

		HeadBucketRequest hbRequest = HeadBucketRequest.builder()
			.bucket(submissionRoot.getBucket())
			.build();
		if (s3Client.headBucket(hbRequest).sdkHttpResponse().isSuccessful()) {
			LOG.info("Bucket '{}' exists", submissionRoot.getBucket());
		} else {
			CreateBucketRequest cbRequest = CreateBucketRequest.builder()
				.bucket(submissionRoot.getBucket())
				.build();
			CreateBucketResponse cbResponse = s3Client.createBucket(cbRequest);
			if (!cbResponse.sdkHttpResponse().isSuccessful()) {
				throw new IllegalStateException(String.format(
					"Unable to create S3 bucket, status code %1$d",
					cbResponse.sdkHttpResponse().statusCode()));
			}
			LOG.info("Created bucket '{}'", submissionRoot.getBucket());
		}
	}

	@PreDestroy
	public void cleanup() {
		s3Client.close();
	}

	@Override
	public InputStream getSubmissionTableAsInputStream(String submissionTableFileName)
			throws FileNotFoundException {
		try {
			GetObjectRequest request = GetObjectRequest.builder()
				.bucket(submissionRoot.getBucket())
				.key(getSubmissionTableKey(submissionTableFileName))
				.build();
			return s3Client.getObject(request);
		} catch (NoSuchKeyException ex) {
			throw new FileNotFoundException(ex.getMessage());
		}
	}

	@Override
	public File getTempSubmissionTableFile(String submissionTableFileName)
			throws IOException {
		File submissionTableFile = new File(submissionTableFileName);
		Pair<String, String> stemExt = FileUtil.getStemExtPair(
			submissionTableFile.getName());
		File tempFile = File.createTempFile(stemExt.getLeft(), "." + stemExt.getRight());
		LOG.debug("Temporary submission table file: '{}'", tempFile.getPath());
		return tempFile;
	}

	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
		justification = "Return value of delete does not matter in this case")
	@Override
	public void transferTempSubmissionTableFile(File tempSubmissionTableFile,
		String submissionTableFileName) throws IOException {
		try {
			PutObjectRequest poRequest = PutObjectRequest.builder()
				.bucket(submissionRoot.getBucket())
				.key(getSubmissionTableKey(submissionTableFileName))
				.build();
			PutObjectResponse response = s3Client.putObject(
				poRequest, tempSubmissionTableFile.toPath());
			if (!response.sdkHttpResponse().isSuccessful()) {
				throw new IOException(String.format(
					"Unable to transfer submission table to S3, status code %1$d",
					response.sdkHttpResponse().statusCode()));
			}
			tempSubmissionTableFile.delete();
		} catch (SdkClientException ex) {
			throw new IOException("Unable to transfer submission table to S3:", ex);
		}
	}

	@Override
	public void transferUploadedFile(MultipartFile file, String eventDirName,
		String newFileName) throws IOException {

		String newFileKey = String.format("%1$s/%2$s/%3$s",
			submissionRoot.getKey(), eventDirName, newFileName);
		PutObjectRequest poRequest = PutObjectRequest.builder()
			.bucket(submissionRoot.getBucket())
			.key(newFileKey)
			.build();
		try (InputStream is = file.getInputStream()) {
			RequestBody requestBody = RequestBody.fromInputStream(is, file.getSize());
			PutObjectResponse response = s3Client.putObject(poRequest, requestBody);
			if (!response.sdkHttpResponse().isSuccessful()) {
				throw new IOException(String.format(
					"Unable to transfer uploaded file to S3, status code %1$d",
					response.sdkHttpResponse().statusCode()));
			}
		} catch (SdkClientException ex) {
			throw new IOException("Unable to transfer uploaded file to S3:", ex);
		}
	}

	private String getSubmissionTableKey(String submissionTableFileName) {
		return String.format("%1$s/%2$s", submissionRoot.getKey(), submissionTableFileName);
	}
}
