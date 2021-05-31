package org.virginiaso.file_upload;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadServiceImpl implements FileUploadService {
	private final List<Tournament> tournamentConfiguration;
	private final EnumMap<Event, EnumMap<Division, EventUploader>> eventUploaders;
	private final AtomicInteger previousSequenceNumber;

	@Autowired
	//@Qualifier("fileSystemStorageService")
	@Qualifier("s3StorageService")
	private StorageService storageService;

	public FileUploadServiceImpl(
		@Value("${fileUpload.timeZone}") String timeZoneStr,
		@Value("${fileUpload.tournamentConfigRsrc}") String tournamentConfigRsrc
	) throws IOException {
		Configuration.setTimeZone(timeZoneStr);
		tournamentConfiguration = Configuration.parse(tournamentConfigRsrc);
		eventUploaders = new EnumMap<>(Event.class);
		previousSequenceNumber = new AtomicInteger(-1);
	}

	/*
	 * This method does not need to be synchronized because it executes
	 * at process start-up, before any client requests are received.
	 */
	@PostConstruct
	public void initialize() {
		for (Event event : Event.values()) {
			EnumMap<Division, EventUploader> subMap = eventUploaders.computeIfAbsent(
				event, key -> new EnumMap<>(Division.class));
			for (Division division : Division.values()) {
				subMap.put(division, new EventUploader(event, division, storageService));
			}
		}
		previousSequenceNumber.set(eventUploaders.values().stream()
			.map(EnumMap::values)
			.flatMap(Collection::stream)
			.mapToInt(EventUploader::getMaxSubmissionId)
			.max()
			.orElse(-1));
	}

	@Override
	public Submission receiveFileUpload(Event event, UserSubmission userSub,
		MultipartFile... files) throws IOException {

		Submission submission = new Submission(userSub, event,
			getNextSequenceNumber(), Instant.now());
		submission.validateTeamAndTime(tournamentConfiguration);
		return getEventUploader(submission).receiveFileUpload(submission, files);
	}

	private EventUploader getEventUploader(Submission submission) {
		return eventUploaders
			.get(submission.getEvent())
			.get(submission.getDivision());
	}

	private int getNextSequenceNumber() {
		return previousSequenceNumber.incrementAndGet();
	}
}
