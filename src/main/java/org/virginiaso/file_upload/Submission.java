package org.virginiaso.file_upload;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virginiaso.file_upload.util.FieldValidationException;
import org.virginiaso.file_upload.util.NoSuchEventException;
import org.virginiaso.file_upload.util.StringUtil;

public final class Submission {
	private static final Logger LOG = LoggerFactory.getLogger(Submission.class);

	private static final ZoneId EASTERN_TZ = ZoneId.of("America/New_York");
	private static final DateTimeFormatter UTC = DateTimeFormatter.ISO_INSTANT;
	private static final DateTimeFormatter ZONED_DATE_TIME = new DateTimeFormatterBuilder()
		.parseStrict()
		.appendValue(ChronoField.YEAR, 4)
		.appendLiteral('-')
		.appendValue(ChronoField.MONTH_OF_YEAR, 2)
		.appendLiteral('-')
		.appendValue(ChronoField.DAY_OF_MONTH, 2)
		.appendLiteral(", at ")
		.appendValue(ChronoField.HOUR_OF_DAY, 2)
		.appendLiteral(':')
		.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
		.appendLiteral(':')
		.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
		.appendLiteral(" (")
		.appendZoneText(TextStyle.SHORT)
		.appendLiteral(')')
		.toFormatter();

	private final Event event;
	private final int id;
	private final Division division;
	private final int teamNumber;
	private final String schoolName;
	private final String teamName;
	private final String studentNames;
	private final String notes;
	private final HelicopterMode helicopterMode;
	private final BigDecimal flightDuration;
	private final String passCode;
	private final List<String> fileNames;
	private final Instant timeStamp;

	public Submission(UserSubmission userSub, String eventTemplate, int id,
		Instant timeStamp) throws NoSuchEventException, FieldValidationException {

		event = Event.forTemplate(eventTemplate);
		this.id = id;
		division = StringUtil.convertEnumerator(Division.class, userSub.getDivision());
		teamNumber = StringUtil.convertInteger(userSub.getTeamNumber());
		schoolName = StringUtil.safeTrim(userSub.getSchoolName());
		teamName = StringUtil.safeTrim(userSub.getTeamName());
		studentNames = StringUtil.safeTrim(userSub.getStudentNames());
		notes = StringUtil.safeTrim(userSub.getNotes());
		helicopterMode = StringUtil.convertEnumerator(
			HelicopterMode.class, userSub.getHelicopterMode());
		flightDuration = StringUtil.convertDecimal(userSub.getFlightDuration());
		passCode = (event == Event.HELICOPTER_START)
			? generatePassCode()
			: null;
		this.timeStamp = Objects.requireNonNull(timeStamp, "timeStamp");
		fileNames = new ArrayList<>();

		validate();
	}

	public Submission(CSVRecord record) throws FieldValidationException {
		event = StringUtil.convertEnumerator(Event.class, record.get(Column.EVENT));
		id = StringUtil.convertInteger(record.get(Column.ID));
		division = StringUtil.convertEnumerator(Division.class, record.get(Column.DIVISION));
		teamNumber = StringUtil.convertInteger(record.get(Column.TEAM_NUMBER));
		schoolName = StringUtil.safeTrim(record.get(Column.SCHOOL_NAME));
		teamName = StringUtil.safeTrim(record.get(Column.TEAM_NAME));
		studentNames = StringUtil.safeTrim(record.get(Column.STUDENT_NAMES));
		notes = StringUtil.safeTrim(record.get(Column.NOTES));
		helicopterMode = StringUtil.convertEnumerator(
			HelicopterMode.class, record.get(Column.HELICOPTER_MODE));
		flightDuration = StringUtil.convertDecimal(record.get(Column.FLIGHT_DURATION));
		passCode = StringUtil.safeTrim(record.get(Column.PASS_CODE));
		timeStamp = Instant.from(UTC.parse(record.get(Column.UTC_TIME_STAMP)));
		fileNames = Column.fileColumns().stream()
			.map(record::get)
			.filter(Objects::nonNull)
			.filter(value -> !value.isBlank())
			.collect(Collectors.toCollection(ArrayList::new));

		validate();
	}

	/**
	 * Generates a random five-to-seven letter string of capital letters A-Z to be
	 * used as the pass code in the Helicopter event.
	 *
	 * @return A random pass code
	 */
	private static String generatePassCode() {
		int numChars = ThreadLocalRandom.current().nextInt(5, 8);
		StringBuilder buffer = new StringBuilder();
		ThreadLocalRandom.current().ints(numChars, 'A', 'Z' + 1)
			.mapToObj(Character::toChars)
			.forEach(charArray -> buffer.append(charArray[0]));
		return buffer.toString();
	}

	public void print(CSVPrinter printer) throws IOException {
		ZonedDateTime zonedTimeStamp = getZonedTimeStamp();

		printer.print(event.name());
		printer.print(Integer.toString(id));
		printer.print(ZONED_DATE_TIME.format(zonedTimeStamp));
		printer.print(division.name());
		printer.print(Integer.toString(teamNumber));
		printer.print(schoolName);
		printer.print(teamName);
		printer.print(studentNames);
		printer.print(notes);
		printer.print((event != Event.HELICOPTER_FINISH || helicopterMode == null)
			? null : helicopterMode.name());
		printer.print((event != Event.HELICOPTER_FINISH || flightDuration == null)
			? null : flightDuration.toPlainString());
		printer.print(passCode);
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
		return ZONED_DATE_TIME.format(zonedTimeStamp);
	}

	// Only used for helicopter:
	public String getFinishTime() {
		ZonedDateTime zonedTimeStamp = getZonedTimeStamp();
		ZonedDateTime zonedFinishTime = zonedTimeStamp.plusHours(1);
		return ZONED_DATE_TIME.format(zonedFinishTime);
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

	public String getFlightDuration() {
		return (flightDuration == null) ? null : flightDuration.toPlainString();
	}

	public String getPassCode() {
		return passCode;
	}

	public List<String> getFileNames() {
		return Collections.unmodifiableList(fileNames);
	}

	public void addFileName(String newFileName) {
		fileNames.add(newFileName);
	}

	public Instant getUtcTimeStamp() {
		return timeStamp;
	}

	public ZonedDateTime getZonedTimeStamp() {
		return ZonedDateTime.ofInstant(timeStamp, EASTERN_TZ);
	}

	private void validate() throws FieldValidationException {
		List<String> errors = new ArrayList<>();

		requireNonNull(errors, event, "the event name (in the URL)");
		requireNonNull(errors, division, "Division");
		if (teamNumber < 1) {
			errors.add("Team Number must be a positive integer");
		}
		requireNonNull(errors, schoolName, "School Name");
		requireNonNull(errors, studentNames, "Student Name(s)");
		if (event == Event.HELICOPTER_FINISH) {
			requireNonNull(errors, helicopterMode, "Kind of Submission");
		}
		if (event == Event.HELICOPTER_START) {
			requireNonNull(errors, passCode, "Pass Code");
		}
		requireNonNull(errors, timeStamp, "timeStamp");

		if (!errors.isEmpty()) {
			String errorMessage = errors.stream()
				.collect(Collectors.joining(String.format("<br/>%n")));
			FieldValidationException ex = new FieldValidationException(errorMessage);
			LOG.error("Validation exception:", ex);
			throw ex;
		}
	}

	private static void requireNonNull(List<String> errors, Object field, String name) {
		if (field == null) {
			errors.add(String.format("%1$s is a required field", name));
		}
	}
}
