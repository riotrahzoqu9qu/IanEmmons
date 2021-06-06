package org.virginiaso.file_upload;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ConfigurationTests {
	@BeforeAll
	public static void beforeAll() {
		Configuration.setTimeZone("America/New_York");
	}

	@ParameterizedTest
	@MethodSource
	public void convertTeamListTest(String inputStr, Set<Integer> expectedTeams) {
		Set<Integer> actualTeams = Configuration.convertTeamList(inputStr);
		assertEquals(expectedTeams, actualTeams);
	}

	private static Stream<Arguments> convertTeamListTest() {
		return Stream.of(
			Arguments.of(
				"12",
				Set.of(12)),
			Arguments.of(
				"  12  ",
				Set.of(12)),
			Arguments.of(
				"3,6,9",
				Set.of(3, 6, 9)),
			Arguments.of(
				" 3, 6, 9 ",
				Set.of(3, 6, 9)),
			Arguments.of(
				"3-6",
				Set.of(3, 4, 5, 6)),
			Arguments.of(
				" 3 - 6 ",
				Set.of(3, 4, 5, 6)),
			Arguments.of(
				"1,3-6,8,10-12,17",
				Set.of(1, 3, 4, 5, 6, 8, 10, 11, 12, 17)),
			Arguments.of(
				"1,8,3-6,17,10-12,11",
				Set.of(1, 3, 4, 5, 6, 8, 10, 11, 12, 17)),
			Arguments.of(
				" 1, 3 - 6, 8, 10 - 12, 17 ",
				Set.of(1, 3, 4, 5, 6, 8, 10, 11, 12, 17))
			);
	}

	@Test
	public void configurationTest() throws IOException {
		List<Tournament> tournaments = Configuration.parse("testTournamentConfig.yaml");
		assertEquals(1, tournaments.size());
		Tournament t = tournaments.get(0);
		assertEquals("Test Regional", t.getName());
		assertEquals("2021-02-06", t.getDate().toString());

		EnumSet<Division> divBC = EnumSet.of(Division.B, Division.C);
		assertEquals(divBC, t.getTeams().keySet());
		Set<Integer> commonTeams = Configuration.convertTeamList("31,33-40,44-59");
		Set<Integer> bOnlyTeams = setDifference(t.getTeams().get(Division.B), commonTeams);
		Set<Integer> cOnlyTeams = setDifference(t.getTeams().get(Division.C), commonTeams);
		assertEquals(Set.of(60), bOnlyTeams);
		assertEquals(Set.of(43), cOnlyTeams);

		assertEquals(EnumSet.of(Event.DETECTOR_DESIGN, Event.VEHICLE_DESIGN, Event.WICI),
			t.getEvents().keySet());
		assertEquals(EnumSet.of(Division.C),
			t.getEvents().get(Event.DETECTOR_DESIGN).keySet());
		assertEquals("2021-02-06T05:00:00Z", getFrom(t, Event.DETECTOR_DESIGN, Division.C));
		assertEquals("2021-02-07T04:59:59Z", getTo(t, Event.DETECTOR_DESIGN, Division.C));
		assertEquals(divBC, t.getEvents().get(Event.VEHICLE_DESIGN).keySet());
		assertEquals("2021-01-17T06:00:00Z", getFrom(t, Event.VEHICLE_DESIGN, Division.B));
		assertEquals("2021-01-30T17:00:00Z", getTo(t, Event.VEHICLE_DESIGN, Division.B));
		assertEquals("2021-01-18T06:00:00Z", getFrom(t, Event.VEHICLE_DESIGN, Division.C));
		assertEquals("2021-01-29T17:00:00Z", getTo(t, Event.VEHICLE_DESIGN, Division.C));
		assertEquals(divBC, t.getEvents().get(Event.WICI).keySet());
		assertEquals("2021-02-06T05:00:00Z", getFrom(t, Event.WICI, Division.B));
		assertEquals("2021-02-07T04:59:59Z", getTo(t, Event.WICI, Division.B));
		assertEquals("2021-02-06T05:00:00Z", getFrom(t, Event.WICI, Division.C));
		assertEquals("2021-02-07T04:59:59Z", getTo(t, Event.WICI, Division.C));
	}

	private static <T> Set<T> setDifference(Set<T> set1, Set<T> set2) {
		Set<T> difference = new HashSet<>(set1);
		difference.removeAll(set2);
		return difference;
	}

	private static String getFrom(Tournament t, Event event, Division division) {
		return t.getEvents().get(event).get(division).getFrom().toString();
	}

	private static String getTo(Tournament t, Event event, Division division) {
		return t.getEvents().get(event).get(division).getTo().toString();
	}
}
