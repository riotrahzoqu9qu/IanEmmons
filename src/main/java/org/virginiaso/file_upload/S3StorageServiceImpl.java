package org.virginiaso.file_upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

@Service("s3StorageService")
public class S3StorageServiceImpl implements StorageService {
	private static final Logger LOG = LoggerFactory.getLogger(S3StorageServiceImpl.class);

	@Value("${fileUpload.aws.region}")
	private String awsRegion;

	@Value("${fileUpload.aws.profile}")
	private String awsProfile;

	@Value("${fileUpload.aws.bucket}")
	private String s3Bucket;

	@Value("${fileUpload.aws.rootKey}")
	private String s3RootKey;

	@Value("${fileUpload.submissionTableFileName}")
	private String submissionTableFileName;

	private AmazonS3 s3Client;

	@PostConstruct
	public void initialize() {
		s3Client = AmazonS3ClientBuilder.standard()
			.withRegion(awsRegion)
			.withCredentials(new ProfileCredentialsProvider(awsProfile))
			.build();

		if(s3Client.doesBucketExistV2(s3Bucket)) {
			LOG.info("Bucket '{}' exists", s3Bucket);
		} else {
			s3Client.createBucket(s3Bucket);
			LOG.info("Created bucket '{}'", s3Bucket);
		}
	}

	@PreDestroy
	public void cleanup() {
		s3Client.shutdown();
	}

	@Override
	public InputStream getSubmissionTableAsInputStream() throws FileNotFoundException {
		try {
			S3Object submissionTable = s3Client.getObject(s3Bucket, getSubmissionTableKey());
			return submissionTable.getObjectContent();
		} catch (SdkClientException ex) {
			throw new FileNotFoundException(ex.getMessage());
		}
	}

	@Override
	public boolean doesSubmissionTableExist() {
		return s3Client.doesObjectExist(s3Bucket, getSubmissionTableKey());
	}

	@Override
	public File getTempSubmissionTableFile() throws IOException {
		Pair<String, String> stemExt = FileUtil.getStemExtPair(submissionTableFileName);
		return File.createTempFile(stemExt.getLeft(), stemExt.getRight());
	}

	@Override
	public void transferTempSubmissionTableFile(File tempSubmissionTableFile) throws IOException {
		try {
			s3Client.putObject(s3Bucket, getSubmissionTableKey(), tempSubmissionTableFile);
		} catch (SdkClientException ex) {
			throw new IOException("Unable to transfer submission table to S3:", ex);
		}
	}

	@Override
	public void transferUploadedFile(MultipartFile file, String eventDirName, String newFileName) throws IOException {
		String newFileKey = "%1$s/%2$s/%3$s".formatted(getSubmissionKey(), eventDirName, newFileName);
		try (InputStream is = file.getInputStream()) {
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentDisposition("attachment; filename=\"%1$s\"".formatted(newFileName));
			metadata.setContentLength(file.getSize());
			s3Client.putObject(s3Bucket, newFileKey, is, metadata);
		}
	}

	private String getSubmissionKey() {
		return "%1$s/%2$s".formatted(s3RootKey,
			DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now()));
	}

	private String getSubmissionTableKey() {
		return "%1$s/%2$s".formatted(getSubmissionKey(), submissionTableFileName);
	}
}
