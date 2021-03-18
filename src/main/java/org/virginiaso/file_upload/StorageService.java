package org.virginiaso.file_upload;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
	Submission receiveFileUpload(String eventTemplate, UserSubmission userSub,
		MultipartFile... files) throws IOException;
}
