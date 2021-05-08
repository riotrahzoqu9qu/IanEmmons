package org.virginiaso.file_upload;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;

final class Tournament {
	@SuppressWarnings("unused")
	private final String name;
	@SuppressWarnings("unused")
	private final Instant date;
	@SuppressWarnings("unused")
	private final EnumMap<Division, List<Integer>> teams;
	@SuppressWarnings("unused")
	private final EnumMap<Event, EnumMap<Division, TimeInterval>> events;

	public Tournament(String name, Instant date, EnumMap<Division, List<Integer>> teams,
			EnumMap<Event, EnumMap<Division, TimeInterval>> events) {
		this.name = name;
		this.date = date;
		this.teams = teams;
		this.events = events;
	}
}
