package org.virginiaso.file_upload;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virginiaso.file_upload.util.ValidationException;

public class SubmissionTests {
	private static final Logger LOG = LoggerFactory.getLogger(SubmissionTests.class);

	@BeforeAll
	public static void beforeAll() {
		Configuration.setTimeZone("America/New_York");
	}

	@ParameterizedTest
	@MethodSource
	public void fieldValidationTest(String eventTemplate, int id, Instant timeStamp,
			UserSubmission userSub, String errorRegex) {
		if (errorRegex == null) {
			assertDoesNotThrow(() -> new Submission(userSub, eventTemplate, id, timeStamp));
		} else if (NullPointerException.class.getSimpleName().equals(errorRegex)) {
			assertThrows(NullPointerException.class,
				() -> new Submission(userSub, eventTemplate, id, timeStamp));
		} else {
			ValidationException ex = assertThrows(ValidationException.class,
				() -> new Submission(userSub, eventTemplate, id, timeStamp));
			LOG.info(ex.getMessage());
			assertTrue(Pattern.matches(errorRegex, ex.getMessage()));
		}
	}

	private static Stream<Arguments> fieldValidationTest() {
		return Stream.of(
			Arguments.of(
				"wici", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				(String) null),
			Arguments.of(
				" wici ", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				(String) null),
			Arguments.of(
				null, 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				"^'null' is not a recognized Science Olympiad event.  Must be one of.*$"),
			Arguments.of(
				"wici", 0, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				(String) null),
			Arguments.of(
				"wici", -1, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				"^Submission ID '-1' must be a non-negative integer$"),
			Arguments.of(
				"wici", 139, null,
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				NullPointerException.class.getSimpleName()),
			Arguments.of(
				"wici", 139, Instant.now(),
				createFieldTest(" B ", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				(String) null),
			Arguments.of(
				"wici", 139, Instant.now(),
				createFieldTest("BC", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				"^Unrecognized Division value 'BC' \\(should be one of B, C\\)$"),
			Arguments.of(
				"wici", 139, Instant.now(),
				createFieldTest("B", " 12 ", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				(String) null),
			Arguments.of(
				"wici", 139, Instant.now(),
				createFieldTest("B", "0", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				"^Team Number '0' must be a positive integer$"),
			Arguments.of(
				"wici", 139, Instant.now(),
				createFieldTest("B", "B12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				"^Ill-formed integer: 'B12'$"),
			Arguments.of(
				"wici", 139, Instant.now(),
				createFieldTest("B", null, "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				"^Ill-formed integer: 'null'$"),
			Arguments.of(
				"wici", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", null, "Kim & Joe",
					null, null, null, null),
				(String) null),
			Arguments.of(
				"helicopterFinish", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				(String) null),
			Arguments.of(
				"helicopterFinish", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", " TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS ", " 37.12 ", " ABCDEF "),
				(String) null),
			Arguments.of(
				"helicopterFinish", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", null, "Kim & Joe",
					null, "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", "ABCDEF"),
				(String) null),
			Arguments.of(
				"helicopterFinish", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "THREE_HELIS", "37.12", "ABCDEF"),
				"^Unrecognized HelicopterMode value 'THREE_HELIS' \\(should be one of.*$"),
			Arguments.of(
				"helicopterFinish", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12 sec", "ABCDEF"),
				"^Ill-formed decimal number: '37\\.12 sec'$"),
			Arguments.of(
				"helicopterFinish", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", "37.12", null),
				"^Unique Word is a required field$"),
			Arguments.of(
				"helicopterFinish", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", "TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS", null, "ABCDEF"),
				"^Ill-formed decimal number: 'null'$"),
			Arguments.of(
				"helicopterFinish", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", null, "37.12", "ABCDEF"),
				"^Unrecognized HelicopterMode value 'null' \\(should be one of.*$"),
			Arguments.of(
				"digitalStructures", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", null, "37.12", "ABCDEF", "13000"),
				null),
			Arguments.of(
				"digitalStructures", 139, Instant.now(),
				createFieldTest("B", "12", "Wandering Minds Academy", "Red", "Kim & Joe",
					"A note", null, "37.12", "ABCDEF", null),
				"^Ill-formed integer: 'null'$"));
	}

	private static UserSubmission createFieldTest(String division, String teamNumber,
			String schoolName, String teamName, String studentNames, String notes,
			String helicopterMode, String flightDuration, String passCode) {
		UserSubmission userSub = new UserSubmission();
		userSub.setDivision(division);
		userSub.setTeamNumber(teamNumber);
		userSub.setSchoolName(schoolName);
		userSub.setTeamName(teamName);
		userSub.setStudentNames(studentNames);
		userSub.setNotes(notes);
		userSub.setHelicopterMode(helicopterMode);
		userSub.setFlightDuration(flightDuration);
		userSub.setPassCode(passCode);
		return userSub;
	}

	private static UserSubmission createFieldTest(String division, String teamNumber,
			String schoolName, String teamName, String studentNames, String notes,
			String helicopterMode, String flightDuration, String passCode,
			String loadEstimate) {
		UserSubmission userSub = createFieldTest(division, teamNumber, schoolName,
			teamName, studentNames, notes, helicopterMode, flightDuration, passCode);
		userSub.setLoadEstimate(loadEstimate);
		return userSub;
	}

	@ParameterizedTest
	@MethodSource
	public void teamTimeValidationTest(Submission submission, String errorRegex) {
		try {
			List<Tournament> tournaments = Configuration.parse("testTournamentConfig2.yaml");
			if (errorRegex == null) {
				assertDoesNotThrow(() -> submission.validateTeamAndTime(tournaments));
			} else {
				ValidationException ex = assertThrows(ValidationException.class,
					() -> submission.validateTeamAndTime(tournaments));
				LOG.info(ex.getMessage());
				assertTrue(Pattern.matches(errorRegex, ex.getMessage()));
			}
		} catch (IOException ex) {
			LOG.error("Error loading configuration:", ex);
			fail(ex);
		}
	}

	private static Stream<Arguments> teamTimeValidationTest() {
		return Stream.of(
			Arguments.of(
				createTeamTimeTest("detectorDesign", "B", "12", "2021-02-06T12:00:00"),
				"^Detector Design is not an event in division B\\.$"),
			Arguments.of(
				createTeamTimeTest("wici", "B", "12", "2021-02-06T12:00:00"),
				"^Team B12 is not competing at any of the tournaments that are accepting "
				+ "Write It/CAD It \\(WICI\\)-B submissions \\(Hook/Leibniz Regional\\)\\.$"),
			Arguments.of(
				createTeamTimeTest("wici", "B", "37", "2021-02-05T23:59:59"),
				"^Write It/CAD It \\(WICI\\)-B is not accepting submissions at this time\\.$"),
			Arguments.of(
				createTeamTimeTest("wici", "B", "37", "2021-02-06T00:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("wici", "B", "37", "2021-02-06T12:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("wici", "B", "37", "2021-02-06T23:59:59"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("wici", "B", "37", "2021-02-07T00:00:00"),
				"^Write It/CAD It \\(WICI\\)-B is not accepting submissions at this time\\.$"),

			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "29", "2021-01-17T00:59:59"),
				"^Vehicle Design-B is not accepting submissions at this time\\.$"),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "29", "2021-01-17T01:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "29", "2021-01-18T12:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "29", "2021-01-23T08:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "29", "2021-01-23T12:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "29", "2021-01-23T12:00:01"),
				"^Team B29 is not competing at any of the tournaments that are accepting "
					+ "Vehicle Design-B submissions \\(Hook/Leibniz Regional\\)\\.$"),

			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "37", "2021-01-17T00:59:59"),
				"^Vehicle Design-B is not accepting submissions at this time\\.$"),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "37", "2021-01-17T01:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "37", "2021-01-18T12:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "37", "2021-01-23T08:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "37", "2021-01-23T12:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "37", "2021-01-23T12:00:01"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "37", "2021-01-30T08:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "37", "2021-01-30T12:00:00"),
				(String) null),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "37", "2021-01-30T12:00:01"),
				"^Vehicle Design-B is not accepting submissions at this time\\.$"),
			Arguments.of(
				createTeamTimeTest("vehicleDesign", "B", "37", "2021-02-07T00:00:00"),
				"^Vehicle Design-B is not accepting submissions at this time\\.$")
			);
	}

	private static Submission createTeamTimeTest(String eventTemplate,
			String division, String teamNumber, String timeStampStr) {
		UserSubmission userSub = new UserSubmission();
		userSub.setDivision(division);
		userSub.setTeamNumber(teamNumber);

		// Set dummy values on required fields that are not involved in this test:
		userSub.setSchoolName("Wandering Minds Academy");
		userSub.setStudentNames("John Doe and Mary Smith");
		userSub.setHelicopterMode(
			HelicopterMode.TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS.toString());
		userSub.setPassCode("ABCDEF");

		Instant timeStamp = LocalDateTime.parse(timeStampStr)
			.atZone(Configuration.getTimeZone())
			.toInstant();

		return new Submission(userSub, eventTemplate, 0, timeStamp);
	}
}
