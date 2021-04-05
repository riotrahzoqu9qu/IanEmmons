package org.virginiaso.file_upload;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Event {
	DETECTOR_BUILDING("detectorBuilding", "Detector Building"),
	HELICOPTER("helicopter", "Helicopter"),
	VEHICLE_DESIGN("vehicleDesign", "Vehicle Design"),
	WICI("wici", "Write It/CAD It (WICI)");

	private final String templateName;
	private final String label;

	private Event(String templateName, String label) {
		this.templateName = Objects.requireNonNull(templateName, "templateName");
		this.label = Objects.requireNonNull(label, "label");
	}

	public String getTemplateName() {
		return templateName;
	}

	public String getLabel() {
		return label;
	}

	public String getName() {
		return name();
	}

	public static Event forTemplate(String template) {
		return Stream.of(Event.values())
			.filter(event -> event.templateName.equals(template))
			.findAny()
			.orElseThrow(() -> {
				String knownEvents = Stream.of(Event.values())
					.map(Event::getTemplateName)
					.collect(Collectors.joining(", "));
				return new IllegalArgumentException(
					String.format("'%1$s' is an unknown event.  Must be one of %2$s",
						template, knownEvents));
			});
	}
}
