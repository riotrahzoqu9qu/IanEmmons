package org.virginiaso.file_upload;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
	Submission receiveFileUpload(String eventTemplate, UserSubmission userSub,
		MultipartFile... files) throws IOException;
}
