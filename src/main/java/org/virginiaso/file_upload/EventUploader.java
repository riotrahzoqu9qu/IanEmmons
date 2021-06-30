package org.virginiaso.file_upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.virginiaso.file_upload.util.FileUtil;
import org.virginiaso.file_upload.util.StreamUtil;

final class EventUploader {
	public static final CSVFormat CSV_FORMAT_IN = CSVFormat.DEFAULT
		.withFirstRecordAsHeader()
		.withTrim()
		.withAllowDuplicateHeaderNames(false);
	public static final CSVFormat CSV_FORMAT_OUT = CSVFormat.DEFAULT
		.withHeader(Column.class)
		.withTrim()
		.withAllowDuplicateHeaderNames(false);
	private static final Logger LOG = LoggerFactory.getLogger(EventUploader.class);

	private final Event event;
	private final Division division;
	private final String eventDirName;
	private final String submissionTableFileName;
	private final StorageService storageService;
	private final List<Submission> submissions;

	public EventUploader(Event event, Division division, StorageService storageService) {
		this.event = Objects.requireNonNull(event, "event");
		this.division = Objects.requireNonNull(division, "division");
		eventDirName = String.format("%1$s-%2$s",
			this.event.getUri(), this.division);
		submissionTableFileName = String.format("%1$s/%2$s-%3$s-submissions.csv",
			eventDirName, this.event.getUri(), this.division);
		this.storageService = Objects.requireNonNull(storageService, "storageService");
		submissions = loadSubmissionsTable(submissionTableFileName, this.storageService);
	}

	private static List<Submission> loadSubmissionsTable(String submissionTableFileName,
		StorageService storageService) {
		try (
			var is = storageService.getSubmissionTableAsInputStream(submissionTableFileName);
			var rdr = new InputStreamReader(is, FileUtil.CHARSET);
			var parser = CSV_FORMAT_IN.parse(rdr);
		) {
			List<Submission> submissions = StreamUtil.from(parser)
				.map(Submission::new)
				.sorted(Comparator.comparingInt(Submission::getId))
				.collect(Collectors.toCollection(ArrayList::new));
			LOG.info("Loaded submissions file {}", submissionTableFileName);
			return submissions;
		} catch (FileNotFoundException ex) {
			LOG.trace("Submissions file {} does not exist -- starting a new one",
				submissionTableFileName);
			return new ArrayList<>();
		} catch (IOException ex) {
			LOG.warn("Unable to load submissions file " + submissionTableFileName + ":", ex);
			throw new UncheckedIOException(ex);
		}
	}

	public int getMaxSubmissionId() {
		return (submissions == null || submissions.isEmpty())
			? -1
			: submissions.get(submissions.size() - 1).getId();
	}

	public Submission receiveFileUpload(Submission submission, MultipartFile[] files)
			throws IOException {
		var label = 'a';
		for (MultipartFile file : files) {
			if (file != null) {
				var fileName = saveUploadedFile(file, submission.getId(),
					Character.toString(label), submission.getEvent(),
					submission.getDivision(), submission.getTeamNumber());
				fileName.ifPresent(submission::addFileName);
			}
			++label;
		}

		addSubmission(submission);

		return submission;
	}

	private Optional<String> saveUploadedFile(MultipartFile file, int id, String label, Event event,
			Division division, int teamNumber) throws IOException {
		if (file.isEmpty()) {
			return Optional.empty();
		}

		var originalFilePath = file.getOriginalFilename();
		if (originalFilePath == null || originalFilePath.isBlank()) {
			originalFilePath = file.getName();
		}
		var originalFileName = new File(originalFilePath).getName();
		Pair<String, String> originalStemExt = FileUtil.getStemExtPair(originalFileName);

		var newFileName = String.format("%1$s%2$d-%3$s-%4$03d%5$s.%6$s",
			division, teamNumber, originalStemExt.getLeft(), id, label,
			originalStemExt.getRight());

		storageService.transferUploadedFile(file, eventDirName, newFileName);

		return Optional.of(newFileName);
	}

	private synchronized void addSubmission(Submission newSubmission) throws IOException {
		// Add the submission to the list:
		submissions.add(newSubmission);

		// Save the list to a temporary file:
		var tempSubmissionTableFile = storageService.getTempSubmissionTableFile(
			submissionTableFileName);
		try (var printer = CSV_FORMAT_OUT.print(tempSubmissionTableFile, FileUtil.CHARSET)) {
			for (var submission : submissions) {
				submission.print(printer);
			}
		}

		// Replace the existing file with the new one:
		storageService.transferTempSubmissionTableFile(tempSubmissionTableFile,
			submissionTableFileName);
	}
}
