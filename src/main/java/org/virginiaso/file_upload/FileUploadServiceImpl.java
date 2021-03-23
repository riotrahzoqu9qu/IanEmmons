package org.virginiaso.file_upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadServiceImpl implements FileUploadService {
	private static final CSVFormat CSV_FORMAT_IN = CSVFormat.DEFAULT
		.withFirstRecordAsHeader()
		.withTrim()
		.withAllowDuplicateHeaderNames(false);
	private static final CSVFormat CSV_FORMAT_OUT = CSVFormat.DEFAULT
		.withHeader(Column.class)
		.withTrim()
		.withAllowDuplicateHeaderNames(false);
	private static final Logger LOG = LoggerFactory.getLogger(FileUploadServiceImpl.class);

	private List<Submission> submissions = new ArrayList<>();
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
		try (
			InputStream is = storageService.getSubmissionTableAsInputStream();
			Reader rdr = new InputStreamReader(is, StandardCharsets.UTF_8);
			CSVParser parser = CSV_FORMAT_IN.parse(rdr);
		) {
			submissions = StreamUtil.from(parser)
				.map(Submission::new)
				.sorted(Comparator.comparingInt(Submission::getId))
				.collect(Collectors.toCollection(ArrayList::new));
			previousSequenceNumber.set(submissions.get(submissions.size() - 1).getId());
			LOG.info("Loaded submissions file");
		} catch (IOException ex) {
			if (storageService.doesSubmissionTableExist()) {
				LOG.warn("Unable to load submissions file:", ex);
			} else {
				LOG.info("Submissions file does not exist -- starting a new one");
			}
			submissions = new ArrayList<>();
			previousSequenceNumber.set(-1);
		}
	}

	private synchronized int getNextSequenceNumber() {
		return previousSequenceNumber.incrementAndGet();
	}

	private synchronized void addSubmission(Submission newSubmission) throws IOException {
		// Add the submission to the list:
		submissions.add(newSubmission);

		// Save the list to a temporary file:
		File tempSubmissionTableFile = storageService.getTempSubmissionTableFile();
		try (CSVPrinter printer = CSV_FORMAT_OUT.print(tempSubmissionTableFile, StandardCharsets.UTF_8)) {
			for (Submission submission : submissions) {
				submission.print(printer);
			}
		}

		// Replace the existing file with the new one:
		storageService.transferTempSubmissionTableFile(tempSubmissionTableFile);
	}

	@Override
	public Submission receiveFileUpload(String eventTemplate, UserSubmission userSub,
		MultipartFile... files) throws IOException {

		int id = getNextSequenceNumber();
		Instant timeStamp = Instant.now();
		Event event = Event.forTemplate(eventTemplate);
		Division division = Division.valueOf(userSub.getDivision());
		int teamNumber = userSub.getTeamNumber();
		List<String> fileNames = new ArrayList<>();
		char label = 'a';
		for (MultipartFile file : files) {
			fileNames.add(saveUploadedFile(file, id, Character.toString(label), event,
				division, teamNumber));
			++label;
		}
		Submission submission = new Submission(userSub, Event.forTemplate(eventTemplate),
			id, timeStamp, fileNames);

		addSubmission(submission);

		return submission;
	}

	private String saveUploadedFile(MultipartFile file, int id, String label, Event event,
		Division division, int teamNumber) throws IOException {

		if (file.isEmpty()) {
			return null;
		}

		String originalFilePath = file.getOriginalFilename();
		if (originalFilePath == null || originalFilePath.isBlank()) {
			originalFilePath = file.getName();
		}
		String originalFileName = new File(originalFilePath).getName();

		String eventDirName = "%1$s-%2$s".formatted(event.getTemplateName(), division);
		String newFileName = "%1$03d%2$s-%3$s%4$d-%5$s".formatted(
			id, label, division, teamNumber, originalFileName);

		storageService.transferUploadedFile(file, eventDirName, newFileName);

		return newFileName;
	}
}
