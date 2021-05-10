package org.virginiaso.file_upload;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.virginiaso.file_upload.util.StringUtil;

public class TimeInterval {
	private final Instant from;
	private final Instant to;

	public TimeInterval(LocalDate tournamentDate) {
		Objects.requireNonNull(tournamentDate, "tournamentDate");
		LocalDateTime ldt = tournamentDate.atTime(0, 0);
		ZonedDateTime zdt = ZonedDateTime.of(ldt, Configuration.getTimeZone());
		Instant startOfDay = zdt.toInstant();
		from = startOfDay;
		to = startOfDay
			.plus(24, ChronoUnit.HOURS)
			.minus(1, ChronoUnit.SECONDS);
	}

	public TimeInterval(String from, String to) {
		this.from = toInstant(from);
		this.to = toInstant(to);
	}

	private static Instant toInstant(String timeStr) {
		if (StringUtil.isBlank(timeStr)) {
			throw new IllegalArgumentException("Blank, empty, or null time string");
		}
		LocalDateTime ldt = LocalDateTime.parse(timeStr.trim());
		ZonedDateTime zdt = ZonedDateTime.of(ldt, Configuration.getTimeZone());
		return zdt.toInstant();
	}

	public Instant getFrom() {
		return from;
	}

	public Instant getTo() {
		return to;
	}

	public boolean contains(Instant time) {
		Objects.requireNonNull(time, "time");
		return from.equals(time)
			|| to.equals(time)
			|| (from.isBefore(time) && to.isAfter(time));
	}
}
