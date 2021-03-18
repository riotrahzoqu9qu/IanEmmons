package org.virginiaso.file_upload;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class Submission {
	private static final ZoneId EASTERN_TZ = ZoneId.of("America/New_York");
	private static final DateTimeFormatter UTC = DateTimeFormatter.ISO_INSTANT;
	private static final DateTimeFormatter ZONED_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter ZONED_TIME = DateTimeFormatter.ISO_LOCAL_TIME;

	private final Event event;
	private final int id;
	private final Division division;
	private final int teamNumber;
	private final String schoolName;
	private final String teamName;
	private final String studentNames;
	private final String notes;
	private final HelicopterMode helicopterMode;
	private final List<String> fileNames;
	private final Instant timeStamp;

	public Submission(UserSubmission userSub, Event event, int id, Instant timeStamp, List<String> fileNames) {
		this.event = event;
		this.id = id;
		division = Division.valueOf(userSub.getDivision());
		teamNumber = userSub.getTeamNumber();
		schoolName = userSub.getSchoolName();
		teamName = userSub.getTeamName();
		studentNames = userSub.getStudentNames();
		notes = userSub.getNotes();
		helicopterMode = convertHelicopterMode(userSub.getHelicopterMode());
		this.timeStamp = timeStamp;
		this.fileNames = fileNames.stream()
			.filter(Objects::nonNull)
			.filter(value -> !value.isBlank())
			.collect(Collectors.toUnmodifiableList());	// defensive copy
	}

	public Submission(CSVRecord record) {
		event = Event.valueOf(record.get(Column.EVENT));
		id = Integer.parseUnsignedInt(record.get(Column.ID));
		division = Division.valueOf(record.get(Column.DIVISION));
		teamNumber = Integer.parseUnsignedInt(record.get(Column.TEAM_NUMBER));
		schoolName = record.get(Column.SCHOOL_NAME);
		teamName = record.get(Column.TEAM_NAME);
		studentNames = record.get(Column.STUDENT_NAMES);
		notes = record.get(Column.NOTES);
		helicopterMode = convertHelicopterMode(record.get(Column.HELICOPTER_MODE));
		timeStamp = Instant.from(UTC.parse(record.get(Column.UTC_TIME_STAMP)));
		fileNames = Column.fileColumns().stream()
			.map(record::get)
			.filter(Objects::nonNull)
			.filter(value -> !value.isBlank())
			.collect(Collectors.toUnmodifiableList());
	}

	private static HelicopterMode convertHelicopterMode(String helicopterModeStr) {
		return (helicopterModeStr == null || helicopterModeStr.isBlank())
			? null
			: HelicopterMode.valueOf(helicopterModeStr);
	}

	public void print(CSVPrinter printer) throws IOException {
		ZonedDateTime zonedTimeStamp = getZonedTimeStamp();

		printer.print(event.name());
		printer.print(Integer.toString(id));
		printer.print(ZONED_DATE.format(zonedTimeStamp));
		printer.print(ZONED_TIME.format(zonedTimeStamp));
		printer.print(division.name());
		printer.print(Integer.toString(teamNumber));
		printer.print(schoolName);
		printer.print(teamName);
		printer.print(studentNames);
		printer.print(notes);
		printer.print((helicopterMode == null) ? null : helicopterMode.name());
		printer.print(UTC.format(timeStamp));
		for (String fileName : fileNames) {
			printer.print(fileName);
		}
		int numEmptyFileColumns = Column.fileColumns().size() - fileNames.size();
		for (int i = 0; i < numEmptyFileColumns; ++i) {
			printer.print(null);
		}
		printer.println();
	}

	public Event getEvent() {
		return event;
	}

	public int getId() {
		return id;
	}

	public String getSubmissionTime() {
		ZonedDateTime zonedTimeStamp = getZonedTimeStamp();
		return "%1$s, %2$s".formatted(
			ZONED_DATE.format(zonedTimeStamp),
			ZONED_TIME.format(zonedTimeStamp));
	}

	public Division getDivision() {
		return division;
	}

	public int getTeamNumber() {
		return teamNumber;
	}

	public String getSchoolName() {
		return schoolName;
	}

	public String getTeamName() {
		return teamName;
	}

	public String getStudentNames() {
		return studentNames;
	}

	public String getNotes() {
		return notes;
	}

	public HelicopterMode getHelicopterMode() {
		return helicopterMode;
	}

	public List<String> getFileNames() {
		return fileNames;
	}

	public Instant getUtcTimeStamp() {
		return timeStamp;
	}

	public ZonedDateTime getZonedTimeStamp() {
		return ZonedDateTime.ofInstant(timeStamp, EASTERN_TZ);
	}
}
