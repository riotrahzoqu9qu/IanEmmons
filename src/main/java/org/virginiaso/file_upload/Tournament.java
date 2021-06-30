package org.virginiaso.file_upload;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Set;

record Tournament(String name, LocalDate date, EnumMap<Division, Set<Integer>> teams,
	EnumMap<Event, EnumMap<Division, TimeInterval>> events) {
}
