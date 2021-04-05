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
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.virginiaso.file_upload.util.FileUtil;
import org.virginiaso.file_upload.util.NoSuchEventException;
import org.virginiaso.file_upload.util.StreamUtil;

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
		}
	}

	@Override
	public Submission receiveFileUpload(String eventTemplate, UserSubmission userSub,
		MultipartFile... files) throws IOException, NoSuchEventException {

		Submission submission = new Submission(userSub, eventTemplate,
			getNextSequenceNumber(), Instant.now());

		char label = 'a';
		for (MultipartFile file : files) {
			if (file != null) {
				String fileName = saveUploadedFile(file, submission.getId(),
					Character.toString(label), submission.getEvent(),
					submission.getDivision(), submission.getTeamNumber());
				if (fileName != null) {
					submission.addFileName(fileName);
				}
			}
			++label;
		}

		addSubmission(submission);

		return submission;
	}

	private int getNextSequenceNumber() {
		return previousSequenceNumber.incrementAndGet();
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
		Pair<String, String> originalStemExt = FileUtil.getStemExtPair(originalFileName);

		String eventDirName = String.format("%1$s-%2$s", event.getTemplateName(), division);
		String newFileName = String.format("%1$s%2$d-%3$s-%4$03d%5$s.%6$s",
			division, teamNumber, originalStemExt.getLeft(), id, label, originalStemExt.getRight());

		storageService.transferUploadedFile(file, eventDirName, newFileName);

		return newFileName;
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
}
