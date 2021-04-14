package org.virginiaso.file_upload;

import java.util.Objects;

final class EventDivisionPair {
	private final Event event;
	private final Division division;

	public EventDivisionPair(Event event, Division division) {
		this.event = Objects.requireNonNull(event, "event");
		this.division = Objects.requireNonNull(division, "division");
	}

	public Event getEvent() {
		return event;
	}

	public Division getDivision() {
		return division;
	}

	@Override
	public int hashCode() {
		return Objects.hash(division, event);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EventDivisionPair)) {
			return false;
		}
		EventDivisionPair other = (EventDivisionPair) obj;
		return division == other.division && event == other.event;
	}

	@Override
	public String toString() {
		return String.format("EventDivisionPair [event=%1$s, division=%2$s]", event, division);
	}
}
