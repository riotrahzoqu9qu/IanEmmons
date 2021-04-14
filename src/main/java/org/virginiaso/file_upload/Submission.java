package org.virginiaso.file_upload;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virginiaso.file_upload.util.FieldValidationException;
import org.virginiaso.file_upload.util.NoSuchEventException;

public class Submission {
	public static final MathContext DURATION_ROUNDING = MathContext.UNLIMITED;
	private static final Logger LOG = LoggerFactory.getLogger(Submission.class);

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
	private BigDecimal flightDuration;
	private final List<String> fileNames;
	private final Instant timeStamp;

	public Submission(UserSubmission userSub, String eventTemplate, int id,
		Instant timeStamp) throws NoSuchEventException, FieldValidationException {

		event = Event.forTemplate(eventTemplate);
		this.id = id;
		division = convertEnumerator(Division.class, userSub.getDivision());
		teamNumber = convertInteger(userSub.getTeamNumber());
		schoolName = safeTrim(userSub.getSchoolName());
		teamName = safeTrim(userSub.getTeamName());
		studentNames = safeTrim(userSub.getStudentNames());
		notes = safeTrim(userSub.getNotes());
		helicopterMode = convertEnumerator(HelicopterMode.class, userSub.getHelicopterMode());
		flightDuration = convertDecimal(userSub.getFlightDuration());
		this.timeStamp = Objects.requireNonNull(timeStamp, "timeStamp");
		fileNames = new ArrayList<>();

		validate();
	}

	public Submission(CSVRecord record) throws FieldValidationException {
		event = convertEnumerator(Event.class, record.get(Column.EVENT));
		id = convertInteger(record.get(Column.ID));
		division = convertEnumerator(Division.class, record.get(Column.DIVISION));
		teamNumber = convertInteger(record.get(Column.TEAM_NUMBER));
		schoolName = safeTrim(record.get(Column.SCHOOL_NAME));
		teamName = safeTrim(record.get(Column.TEAM_NAME));
		studentNames = safeTrim(record.get(Column.STUDENT_NAMES));
		notes = safeTrim(record.get(Column.NOTES));
		helicopterMode = convertEnumerator(HelicopterMode.class, record.get(Column.HELICOPTER_MODE));
		flightDuration = convertDecimal(record.get(Column.FLIGHT_DURATION));
		timeStamp = Instant.from(UTC.parse(record.get(Column.UTC_TIME_STAMP)));
		fileNames = Column.fileColumns().stream()
			.map(record::get)
			.filter(Objects::nonNull)
			.filter(value -> !value.isBlank())
			.collect(Collectors.toCollection(ArrayList::new));

		validate();
	}

	private static <E extends Enum<E>> E convertEnumerator(Class<E> enumClass, String enumStr) {
		Objects.requireNonNull(enumClass, "enumClass");
		try {
			return (enumStr == null || enumStr.isBlank())
				? null
				: E.valueOf(enumClass, enumStr.trim());
		} catch (IllegalArgumentException ex) {
			LOG.warn("Bad {} enum value '{}'", enumClass.getSimpleName(), enumStr);
			return null;
		}
	}

	private static int convertInteger(String integerStr) {
		try {
			return (integerStr == null || integerStr.isBlank())
				? -1
				: Integer.parseUnsignedInt(integerStr.trim());
		} catch (NumberFormatException ex) {
			LOG.warn("Bad integer value '{}'", integerStr);
			return -1;
		}
	}

	private static BigDecimal convertDecimal(String decimalStr) {
		try {
			return (decimalStr == null || decimalStr.isBlank())
				? null
				: new BigDecimal(decimalStr.trim(), DURATION_ROUNDING);
		} catch (NumberFormatException ex) {
			LOG.warn("Bad decimal value '{}'", decimalStr);
			return null;
		}
	}

	private static String safeTrim(String decimalStr) {
		return (decimalStr == null || decimalStr.isBlank())
			? null
			: decimalStr.trim();
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
		printer.print((event != Event.HELICOPTER || helicopterMode == null)
			? null : helicopterMode.name());
		printer.print((event != Event.HELICOPTER || flightDuration == null)
			? null : flightDuration.toPlainString());
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
		return String.format("%1$s, at %2$s",
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

	public String getFlightDuration() {
		return flightDuration.toPlainString();
	}

	public void setflightDuration(String flightDuration) {
		this.flightDuration = new BigDecimal(flightDuration, DURATION_ROUNDING);
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
		if (event == Event.HELICOPTER) {
			requireNonNull(errors, helicopterMode, "Kind of Submission");
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

	private static void requireNonNull(List<String> errors, Object field, String fieldName) {
		if (field == null) {
			errors.add(String.format("%1$s is a required field", fieldName));
		}
	}
}
