package org.virginiaso.file_upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageServiceImpl implements StorageService {
	private static final CSVFormat CSV_FORMAT_IN = CSVFormat.DEFAULT
		.withFirstRecordAsHeader()
		.withTrim()
		.withAllowDuplicateHeaderNames(false);
	private static final CSVFormat CSV_FORMAT_OUT = CSVFormat.DEFAULT
		.withHeader(Column.class)
		.withTrim()
		.withAllowDuplicateHeaderNames(false);
	private static final Logger LOG = LoggerFactory.getLogger(StorageServiceImpl.class);

	@Value("${fileUpload.submissionTablePath}")
	private String submissionTablePath;

	private File submissionTable;
	private List<Submission> submissions;
	private AtomicInteger lastSequenceNumber = new AtomicInteger(0);

	@PostConstruct
	public void initialize() {
		submissionTable = new File(submissionTablePath);

		try {
			load();
		} catch (IOException ex) {
			if (submissionTable.exists()) {
				LOG.warn("Unable to load submissions:", ex);
			} else {
				LOG.info("Submissions file does not exist -- starting a new one");
			}
			submissions = new ArrayList<>();
			lastSequenceNumber.set(0);
		}
	}

	private void load() throws FileNotFoundException, IOException {
		try (
			Reader rdr = new FileReader(submissionTable, StandardCharsets.UTF_8);
			CSVParser parser = CSV_FORMAT_IN.parse(rdr);
		) {
			submissions = StreamUtil.from(parser)
				.map(Submission::new)
				.sorted(Comparator.comparingInt(Submission::getId))
				.collect(Collectors.toCollection(ArrayList::new));
			lastSequenceNumber.set(submissions.get(submissions.size() - 1).getId());
		}
	}

	private void save() throws IOException {
		File tempSaveFile = FileUtil.appendToFileStem(submissionTable, "-temp");
		File oldSaveFile = FileUtil.appendToFileStem(submissionTable, "-old");

		try (CSVPrinter printer = CSV_FORMAT_OUT.print(tempSaveFile, StandardCharsets.UTF_8)) {
			for (Submission submission : submissions) {
				submission.print(printer);
			}
		}

		if (submissionTable.exists()) {
			Files.move(submissionTable.toPath(), oldSaveFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
		}
		Files.move(tempSaveFile.toPath(), submissionTable.toPath(), StandardCopyOption.ATOMIC_MOVE);
		if (oldSaveFile.exists()) {
			oldSaveFile.delete();
		}
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
			fileNames.add(saveFile(file, id, Character.toString(label), event, division, teamNumber));
			++label;
		}
		Submission submission = new Submission(userSub, Event.forTemplate(eventTemplate), id, timeStamp, fileNames);

		submissions.add(submission);
		save();

		return submission;
	}

	private int getNextSequenceNumber() {
		return lastSequenceNumber.incrementAndGet();
	}

	private String saveFile(MultipartFile file, int id, String label, Event event,
		Division division, int teamNumber) throws IOException {

		if (file.isEmpty()) {
			return null;
		}

		String originalFilePath = file.getOriginalFilename();
		if (originalFilePath == null || originalFilePath.isBlank()) {
			originalFilePath = file.getName();
		}
		String originalFileName = new File(originalFilePath).getName();

		File submissionDir = submissionTable.getParentFile();
		submissionDir = new File(submissionDir, "%1$s-%2$s".formatted(
			event.getTemplateName(), division));
		File newPath = new File(submissionDir,
			"%1$03d%2$s-%3$s%4$d-%5$s".formatted(id, label, division, teamNumber, originalFileName));

		if (!submissionDir.isDirectory()) {
			submissionDir.mkdirs();
		}
		file.transferTo(newPath);

		return newPath.getName();
	}
}
