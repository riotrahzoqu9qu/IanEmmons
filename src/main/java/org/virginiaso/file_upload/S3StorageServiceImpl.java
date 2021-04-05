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

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service("s3StorageService")
public class S3StorageServiceImpl implements StorageService {
	private static final Logger LOG = LoggerFactory.getLogger(S3StorageServiceImpl.class);

	@Value("${fileUpload.aws.region}")
	private String awsRegion;

	@Value("${fileUpload.aws.profile}")
	private String awsProfile;

	@Value("${fileUpload.aws.submissionBucket}")
	private String s3SubmissionBucket;

	@Value("${fileUpload.aws.submissionRootKey}")
	private String s3SubmissionRootKey;

	@Value("${fileUpload.submissionTableFileName}")
	private String submissionTableFileName;

	private S3Client s3Client;

	@PostConstruct
	public void initialize() {
		s3Client = S3Client.builder()
			.credentialsProvider(ProfileCredentialsProvider.create(awsProfile))
			.region(Region.of(awsRegion))
			.build();

		HeadBucketRequest hbRequest = HeadBucketRequest.builder()
			.bucket(s3SubmissionBucket)
			.build();
		if(s3Client.headBucket(hbRequest).sdkHttpResponse().isSuccessful()) {
			LOG.info("Bucket '{}' exists", s3SubmissionBucket);
		} else {
			CreateBucketRequest cbRequest = CreateBucketRequest.builder()
				.bucket(s3SubmissionBucket)
				.build();
			CreateBucketResponse cbResponse = s3Client.createBucket(cbRequest);
			if (!cbResponse.sdkHttpResponse().isSuccessful()) {
				throw new IllegalStateException(String.format(
					"Unable to create S3 bucket, status code %1$d",
					cbResponse.sdkHttpResponse().statusCode()));
			}
			LOG.info("Created bucket '{}'", s3SubmissionBucket);
		}
	}

	@PreDestroy
	public void cleanup() {
		s3Client.close();
	}

	@Override
	public InputStream getSubmissionTableAsInputStream() throws FileNotFoundException {
		try {
			GetObjectRequest request = GetObjectRequest.builder()
				.bucket(s3SubmissionBucket)
				.key(getSubmissionTableKey())
				.build();
			return s3Client.getObject(request);
		} catch (SdkClientException | NoSuchKeyException ex) {
			throw new FileNotFoundException(ex.getMessage());
		}
	}

	@Override
	public boolean doesSubmissionTableExist() {
		HeadObjectRequest hoRequest = HeadObjectRequest.builder()
			.bucket(s3SubmissionBucket)
			.key(getSubmissionTableKey())
			.build();
		try {
			return s3Client.headObject(hoRequest).sdkHttpResponse().isSuccessful();
		} catch (NoSuchKeyException ex) {
			return false;
		}
	}

	@Override
	public File getTempSubmissionTableFile() throws IOException {
		Pair<String, String> stemExt = FileUtil.getStemExtPair(submissionTableFileName);
		return File.createTempFile(stemExt.getLeft(), stemExt.getRight());
	}

	@Override
	public void transferTempSubmissionTableFile(File tempSubmissionTableFile) throws IOException {
		try {
			PutObjectRequest poRequest = PutObjectRequest.builder()
				.bucket(s3SubmissionBucket)
				.key(getSubmissionTableKey())
				.build();
			PutObjectResponse response = s3Client.putObject(poRequest, tempSubmissionTableFile.toPath());
			if (!response.sdkHttpResponse().isSuccessful()) {
				throw new IOException(String.format(
					"Unable to transfer submission table to S3, status code %1$d",
					response.sdkHttpResponse().statusCode()));
			}
		} catch (SdkClientException ex) {
			throw new IOException("Unable to transfer submission table to S3:", ex);
		}
	}

	@Override
	public void transferUploadedFile(MultipartFile file, String eventDirName,
		String newFileName) throws IOException {

		String newFileKey = String.format("%1$s/%2$s/%3$s",
			getSubmissionRootKey(), eventDirName, newFileName);
		PutObjectRequest poRequest = PutObjectRequest.builder()
			.bucket(s3SubmissionBucket)
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

	private String getSubmissionRootKey() {
		return s3SubmissionRootKey;
	}

	private String getSubmissionTableKey() {
		return String.format("%1$s/%2$s", getSubmissionRootKey(), submissionTableFileName);
	}
}
