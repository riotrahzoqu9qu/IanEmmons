package org.virginiaso.file_upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.virginiaso.file_upload.util.FileUtil;

@Service("fileSystemStorageService")
public class FileSystemStorageServiceImpl implements StorageService {
	@Value("${fileUpload.localFileSystem.submissionRootDir}")
	private String submissionRootDir;

	@Value("${fileUpload.submissionTableFileName}")
	private String submissionTableFileName;

	@Override
	public InputStream getSubmissionTableAsInputStream() throws FileNotFoundException {
		return new FileInputStream(getSubmissionTableFile());
	}

	@Override
	public boolean doesSubmissionTableExist() {
		return getSubmissionTableFile().exists();
	}

	@Override
	public File getTempSubmissionTableFile() throws IOException {
		Pair<String, String> stemExt = FileUtil.getStemExtPair(submissionTableFileName);
		return File.createTempFile(stemExt.getLeft(), stemExt.getRight(), getSubmissionDir());
	}

	// Replace the existing file by swapping file names and then deleting the old file:
	@Override
	public void transferTempSubmissionTableFile(File tempSubmissionTableFile) throws IOException {
		File submissionTable = getSubmissionTableFile();
		File savedSubmissionTableFile = FileUtil.appendToFileStem(submissionTable, "-old");
		if (submissionTable.exists()) {
			FileUtil.move(submissionTable, savedSubmissionTableFile);
		}
		FileUtil.move(tempSubmissionTableFile, submissionTable);
		if (savedSubmissionTableFile.exists()) {
			savedSubmissionTableFile.delete();
		}
	}

	@Override
	public void transferUploadedFile(MultipartFile file, String eventDirName, String newFileName) throws IOException {
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

	private File getSubmissionTableFile() {
		return new File(getSubmissionDir(), submissionTableFileName);
	}
}
