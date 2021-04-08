package org.virginiaso.file_upload;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.virginiaso.file_upload.util.NoSuchEventException;

@Service
public class FileUploadServiceImpl implements FileUploadService {
	private Map<EventDivisionPair, EventUploader> eventUploaders;
	private final AtomicInteger previousSequenceNumber = new AtomicInteger(-1);

	@Autowired
	//@Qualifier("fileSystemStorageService")
	@Qualifier("s3StorageService")
	private StorageService storageService;

	/*
	 * This method does not need to be synchronized because it executes
	 * at process start-up, before any client requests are received.
	 */
	@PostConstruct
	public void initialize() {
		eventUploaders = new HashMap<>();
		for (Event event : Event.values()) {
			for (Division division : Division.values()) {
				eventUploaders.put(
					new EventDivisionPair(event, division),
					new EventUploader(event, division, storageService));
			}
		}
		previousSequenceNumber.set(eventUploaders.values().stream()
			.mapToInt(EventUploader::getMaxSubmissionId)
			.max()
			.orElse(-1));
	}

	@Override
	public Submission receiveFileUpload(String eventTemplate, UserSubmission userSub,
		MultipartFile... files) throws IOException, NoSuchEventException {

		Submission submission = new Submission(userSub, eventTemplate,
			getNextSequenceNumber(), Instant.now());
		return getEventUploader(submission).receiveFileUpload(submission, files);
	}

	private EventUploader getEventUploader(Submission submission) {
		EventDivisionPair eventDiv = new EventDivisionPair(
			submission.getEvent(),
			submission.getDivision());
		return eventUploaders.get(eventDiv);
	}

	private int getNextSequenceNumber() {
		return previousSequenceNumber.incrementAndGet();
	}
}
