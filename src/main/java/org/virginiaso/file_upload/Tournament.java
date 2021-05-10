package org.virginiaso.file_upload;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Set;

final class Tournament {
	private final String name;
	private final LocalDate date;
	private final EnumMap<Division, Set<Integer>> teams;
	private final EnumMap<Event, EnumMap<Division, TimeInterval>> events;

	public Tournament(String name, LocalDate date, EnumMap<Division, Set<Integer>> teams,
			EnumMap<Event, EnumMap<Division, TimeInterval>> events) {
		this.name = name;
		this.date = date;
		this.teams = teams;
		this.events = events;
	}

	public String getName() {
		return name;
	}

	public LocalDate getDate() {
		return date;
	}

	public EnumMap<Division, Set<Integer>> getTeams() {
		return teams;
	}

	public EnumMap<Event, EnumMap<Division, TimeInterval>> getEvents() {
		return events;
	}
}
