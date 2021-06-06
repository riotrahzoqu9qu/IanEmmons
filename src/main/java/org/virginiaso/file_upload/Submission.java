package org.virginiaso.file_upload;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
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
import org.virginiaso.file_upload.util.StringUtil;
import org.virginiaso.file_upload.util.ValidationException;

public final class Submission {
	private static final Logger LOG = LoggerFactory.getLogger(Submission.class);

	private static final DateTimeFormatter UTC = DateTimeFormatter.ISO_INSTANT;
	private static final DateTimeFormatter ZONED_DATE_TIME = new DateTimeFormatterBuilder()
		.parseStrict()
		.appendValue(ChronoField.MONTH_OF_YEAR, 2)
		.appendLiteral('/')
		.appendValue(ChronoField.DAY_OF_MONTH, 2)
		.appendLiteral('/')
		.appendValue(ChronoField.YEAR, 4)
		.appendLiteral(' ')
		.appendValue(ChronoField.CLOCK_HOUR_OF_AMPM, 2)
		.appendLiteral(':')
		.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
		.appendLiteral(':')
		.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
		.appendLiteral(' ')
		.appendText(ChronoField.AMPM_OF_DAY, TextStyle.FULL)
		.toFormatter();

	private final Event event;
	private final int id;
	private final Division division;
	private final int teamNumber;
	private final String schoolName;
	private final String teamName;
	private final String studentNames;
	private final NotesUploadMode notesUploadMode;
	private final HelicopterMode helicopterMode;
	private final BigDecimal flightDuration;
	private final String passCode;
	private final int loadEstimate;
	private final String notes;
	private final List<String> fileNames;
	private final Instant timeStamp;

	public Submission(UserSubmission userSub, Event event, int id, Instant timeStamp) {
		this.event = event;
		this.id = id;
		division = StringUtil.convertEnumerator(Division.class, userSub.getDivision());
		teamNumber = StringUtil.convertInteger(userSub.getTeamNumber());
		schoolName = StringUtil.safeTrim(userSub.getSchoolName());
		teamName = StringUtil.safeTrim(userSub.getTeamName());
		studentNames = StringUtil.safeTrim(userSub.getStudentNames());
		notesUploadMode = event.isNotesUpload()
			? StringUtil.convertEnumerator(NotesUploadMode.class, userSub.getNotesUploadMode())
			: null;
		helicopterMode = (event == Event.HELICOPTER_FINISH)
			? StringUtil.convertEnumerator(HelicopterMode.class, userSub.getHelicopterMode())
			: null;
		flightDuration = (event == Event.HELICOPTER_FINISH)
			? StringUtil.convertDecimal(userSub.getFlightDuration())
			: null;
		passCode = (event == Event.HELICOPTER_START)
			? generatePassCode()
			: StringUtil.safeTrim(userSub.getPassCode());
		loadEstimate = (event == Event.DIGITAL_STRUCTURES)
			? StringUtil.convertInteger(userSub.getLoadEstimate())
			: -1;
		notes = StringUtil.safeTrim(userSub.getNotes());
		fileNames = new ArrayList<>();
		this.timeStamp = Objects.requireNonNull(timeStamp, "timeStamp");

		validateFields();
	}

	public Submission(CSVRecord record) {
		event = StringUtil.convertEnumerator(Event.class, record.get(Column.EVENT));
		id = StringUtil.convertInteger(record.get(Column.ID));
		schoolName = StringUtil.safeTrim(record.get(Column.SCHOOL_NAME));
		teamName = StringUtil.safeTrim(record.get(Column.TEAM_NAME));
		studentNames = StringUtil.safeTrim(record.get(Column.STUDENT_NAMES));
		notesUploadMode = event.isNotesUpload()
			? StringUtil.convertEnumerator(NotesUploadMode.class,
				record.get(Column.NOTES_UPLOAD_MODE))
			: null;
		helicopterMode = (event == Event.HELICOPTER_FINISH)
			? StringUtil.convertEnumerator(HelicopterMode.class,
				record.get(Column.HELICOPTER_MODE))
			: null;
		flightDuration = (event == Event.HELICOPTER_FINISH)
			? StringUtil.convertDecimal(record.get(Column.FLIGHT_DURATION))
			: null;
		passCode = StringUtil.safeTrim(record.get(Column.PASS_CODE));
		loadEstimate = (event == Event.DIGITAL_STRUCTURES)
			? StringUtil.convertInteger(record.get(Column.LOAD_ESTIMATE))
			: -1;
		notes = StringUtil.safeTrim(record.get(Column.NOTES));
		fileNames = Column.fileColumns().stream()
			.map(record::get)
			.filter(Objects::nonNull)
			.filter(value -> !value.isBlank())
			.collect(Collectors.toCollection(ArrayList::new));
		timeStamp = Instant.from(UTC.parse(record.get(Column.UTC_TIME_STAMP)));
		division = StringUtil.convertEnumerator(Division.class, record.get(Column.DIVISION));
		teamNumber = StringUtil.convertInteger(record.get(Column.TEAM_NUMBER));

		validateFields();
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
		printer.print(String.format("%1$s%2$d", division, teamNumber));
		printer.print(schoolName);
		printer.print(teamName);
		printer.print(studentNames);
		printer.print((!event.isNotesUpload() || notesUploadMode == null)
			? null : notesUploadMode.name());
		printer.print((event != Event.HELICOPTER_FINISH || helicopterMode == null)
			? null : helicopterMode.name());
		printer.print((event != Event.HELICOPTER_FINISH || flightDuration == null)
			? null : flightDuration.toPlainString());
		printer.print(passCode);
		printer.print((event != Event.DIGITAL_STRUCTURES || loadEstimate < 0)
			? null : Integer.toString(loadEstimate));
		printer.print(notes);
		for (String fileName : fileNames) {
			printer.print(fileName);
		}
		int numEmptyFileColumns = Column.fileColumns().size() - fileNames.size();
		for (int i = 0; i < numEmptyFileColumns; ++i) {
			printer.print(null);
		}
		printer.print(UTC.format(timeStamp));
		printer.print(division.name());
		printer.print(Integer.toString(teamNumber));
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

	public NotesUploadMode getNotesUploadMode() {
		return notesUploadMode;
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

	public int getLoadEstimate() {
		return loadEstimate;
	}

	public String getNotes() {
		return notes;
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
		return ZonedDateTime.ofInstant(timeStamp, Configuration.getTimeZone());
	}

	/**
	 * Checks that the individual fields of this submission are valid, e.g.,
	 * required fields are not missing, numbers in range, enumerated values are
	 * proper, etc.
	 *
	 * @throws ValidationException if the submission is invalid. The exception
	 *                             message will contain an explanation of why.
	 */
	private void validateFields() {
		List<String> errors = new ArrayList<>();

		requireNonNull(errors, event, "the event name (in the URL)");
		if (id < 0) {
			addError(errors, "Submission ID '%1$d' must be a non-negative integer", id);
		}
		requireNonNull(errors, division, "Division");
		if (!event.isOfferedIn(division)) {
			addError(errors, "%1$s is not offered in Division %2$s",
				event.getLabel(), division.toString());
		}
		if (teamNumber < 1) {
			addError(errors, "Team Number '%1$d' must be a positive integer", teamNumber);
		}
		requireNonNull(errors, schoolName, "School Name");
		requireNonNull(errors, studentNames, "Student Name(s)");
		if (event.isNotesUpload()) {
			requireNonNull(errors, notesUploadMode, "Kind of Submission");
		}
		if (event == Event.HELICOPTER_FINISH) {
			requireNonNull(errors, helicopterMode, "Kind of Submission");
		}
		if (event == Event.HELICOPTER_START || event == Event.HELICOPTER_FINISH) {
			requireNonNull(errors, passCode, "Unique Word");
		}
		if (event == Event.DIGITAL_STRUCTURES && loadEstimate < 0) {
			addError(errors, "Estimated Load Supported %1$d must be a non-negative integer",
				loadEstimate);
		}
		requireNonNull(errors, timeStamp, "timeStamp");

		if (!errors.isEmpty()) {
			String errorMessage = errors.stream()
				.collect(Collectors.joining(String.format("<br/>%n")));
			LOG.warn("Validation error message: {}", errorMessage);
			throw new ValidationException(errorMessage);
		}
	}

	private static void requireNonNull(List<String> errors, Object field, String name) {
		if (field == null) {
			addError(errors, "%1$s is a required field", name);
		}
	}

	private static void addError(List<String> errors, String format, Object... args) {
		errors.add(String.format(format, args));
	}

	/**
	 * Checks that this submission is valid according to the given tournaments
	 * (typically loaded from the configuration). This checks that the team number
	 * is valid in the given division and that the submission is within the time
	 * limits for the declared event and division.
	 *
	 * @param tournaments The list of tournaments against which to check
	 * @throws ValidationException if the submission is invalid. The exception
	 *                             message will contain an explanation of why.
	 */
	public void validateTeamAndTime(List<Tournament> tournaments) {
		// Check whether the given event and division are a thing:
		List<Tournament> tournamentsWithEventAndDiv = tournaments.stream()
			.filter(t -> t.getEvents().containsKey(getEvent()))
			.filter(t -> t.getEvents().get(getEvent()).containsKey(getDivision()))
			.collect(Collectors.toList());
		if (tournamentsWithEventAndDiv.isEmpty()) {
			throw new ValidationException("%1$s is not an event in division %2$s.",
				getEvent().getLabel(), getDivision());
		}

		// Find the tournaments that are open for submissions in this
		// submission's event and division and at this submission's time:
		List<Tournament> tournamentsAtTime = tournamentsWithEventAndDiv.stream()
			.filter(t -> t.getEvents().get(getEvent()).get(getDivision()).contains(
				getUtcTimeStamp()))
			.collect(Collectors.toList());
		if (tournamentsAtTime.isEmpty()) {
			throw new ValidationException(
				"%1$s-%2$s is not accepting submissions at this time.",
				getEvent().getLabel(), getDivision());
		}

		// Now winnow the list to those that have this submission's team:
		List<Tournament> tournamentsWithTeam = tournamentsAtTime.stream()
			.filter(t -> t.getTeams().containsKey(getDivision()))
			.filter(t -> t.getTeams().get(getDivision()).contains(getTeamNumber()))
			.collect(Collectors.toList());
		if (tournamentsWithTeam.isEmpty()) {
			String tournamentNameList = tournamentsAtTime.stream()
				.map(t -> t.getName())
				.collect(Collectors.joining(", "));
			throw new ValidationException("Team %1$s%2$d is not competing at any of "
				+ "the tournaments that are accepting %3$s-%1$s submissions (%4$s).",
				getDivision(), teamNumber, getEvent().getLabel(), tournamentNameList);
		}
	}
}
