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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Service("fileSystemStorageService")
public class FileSystemStorageServiceImpl implements StorageService {
	private static final Logger LOG = LoggerFactory.getLogger(
		FileSystemStorageServiceImpl.class);

	@Value("${fileUpload.localFileSystem.submissionRootDir}")
	private String submissionRootDir;

	@Override
	public InputStream getSubmissionTableAsInputStream(String submissionTableFileName)
			throws FileNotFoundException {
		return new FileInputStream(getSubmissionTableFile(submissionTableFileName));
	}

	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
		justification = "Return value of mkdirs does not matter in this case")
	@Override
	public File getTempSubmissionTableFile(String submissionTableFileName)
			throws IOException {
		File submissionTableFile = getSubmissionTableFile(submissionTableFileName);
		File submissionTableDir = submissionTableFile.getParentFile();
		if (!submissionTableDir.isDirectory()) {
			submissionTableDir.mkdirs();
		}
		Pair<String, String> stemExt = FileUtil.getStemExtPair(
			submissionTableFile.getName());
		File tempFile = File.createTempFile(stemExt.getLeft(), "." + stemExt.getRight(),
			submissionTableDir);
		LOG.debug("Temporary submission table file: '{}'", tempFile.getPath());
		return tempFile;
	}

	// Replace the existing file by swapping file names and then deleting the old file:
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
		justification = "Return value of mkdirs does not matter in this case")
	@Override
	public void transferTempSubmissionTableFile(File tempSubmissionTableFile,
		String submissionTableFileName) throws IOException {

		File submissionTableFile = getSubmissionTableFile(submissionTableFileName);
		File oldSubmissionTableFile = FileUtil.appendToFileStem(
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

	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
		justification = "Return value of mkdirs does not matter in this case")
	@Override
	public void transferUploadedFile(MultipartFile file, String eventDirName,
		String newFileName) throws IOException {

		File eventDir = new File(getSubmissionDir(), eventDirName);
		if (!eventDir.isDirectory()) {
			eventDir.mkdirs();
		}
		File newPath = new File(eventDir, newFileName);
		file.transferTo(newPath);
	}

	private File getSubmissionDir() {
		return new File(submissionRootDir);
	}

	private File getSubmissionTableFile(String submissionTableFileName) {
		return new File(getSubmissionDir(), submissionTableFileName);
	}
}
