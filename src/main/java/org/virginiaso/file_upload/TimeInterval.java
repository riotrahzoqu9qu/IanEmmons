package org.virginiaso.file_upload;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class TimeInterval {
	private final Instant from;
	private final Instant to;

	public TimeInterval(Instant startOfDay) {
		from = startOfDay;
		to = startOfDay
			.plus(24, ChronoUnit.HOURS)
			.minus(1, ChronoUnit.MINUTES);
	}

	public TimeInterval(String from, String to) {
		this.from = toInstant(from);
		this.to = toInstant(to);
	}

	private static Instant toInstant(String timeStr) {
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
}
