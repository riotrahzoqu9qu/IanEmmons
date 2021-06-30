package org.virginiaso.file_upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.virginiaso.file_upload.util.FileUtil;

@Service("fileSystemStorageService")
public class FileSystemStorageServiceImpl implements StorageService {
	private static final Logger LOG = LoggerFactory.getLogger(
		FileSystemStorageServiceImpl.class);

	private final File submissionRootDir;

	public FileSystemStorageServiceImpl(
		@Value("${fileUpload.localFileSystem.submissionRoot}") String submissionRoot) {
		submissionRootDir = new File(submissionRoot);
	}

	@Override
	public InputStream getSubmissionTableAsInputStream(String submissionTableFileName)
			throws FileNotFoundException {
		return new FileInputStream(getSubmissionTableFile(submissionTableFileName));
	}

	@Override
	public File getTempSubmissionTableFile(String submissionTableFileName)
			throws IOException {
		var submissionTableFile = getSubmissionTableFile(submissionTableFileName);
		var submissionTableDir = submissionTableFile.getParentFile();
		if (!submissionTableDir.isDirectory()) {
			submissionTableDir.mkdirs();
		}
		Pair<String, String> stemExt = FileUtil.getStemExtPair(
			submissionTableFile.getName());
		var tempFile = File.createTempFile(stemExt.getLeft(), "." + stemExt.getRight(),
			submissionTableDir);
		LOG.debug("Temporary submission table file: '{}'", tempFile.getPath());
		return tempFile;
	}

	// Replace the existing file by swapping file names and then deleting the old file:
	@Override
	public void transferTempSubmissionTableFile(File tempSubmissionTableFile,
		String submissionTableFileName) throws IOException {

		var submissionTableFile = getSubmissionTableFile(submissionTableFileName);
		var oldSubmissionTableFile = FileUtil.appendToFileStem(
			submissionTableFile, "-old");
		if (submissionTableFile.exists()) {
			FileUtil.move(submissionTableFile, oldSubmissionTableFile);
		} else if (!submissionTableFile.getParentFile().isDirectory()) {
			submissionTableFile.getParentFile().mkdirs();
		}
		FileUtil.move(tempSubmissionTableFile, submissionTableFile);
		if (oldSubmissionTableFile.exists()) {
			oldSubmissionTableFile.delete();
		}
	}

	@Override
	public void transferUploadedFile(MultipartFile file, String eventDirName,
		String newFileName) throws IOException {

		var eventDir = new File(submissionRootDir, eventDirName);
		if (!eventDir.isDirectory()) {
			eventDir.mkdirs();
		}
		var newPath = new File(eventDir, newFileName);
		file.transferTo(newPath);
	}

	private File getSubmissionTableFile(String submissionTableFileName) {
		return new File(submissionRootDir, submissionTableFileName);
	}
}
