package org.virginiaso.file_upload;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.virginiaso.file_upload.util.NoSuchEventException;

enum Event {
	DETECTOR_DESIGN("detectorDesign", "Detector Design"),
	HELICOPTER_START("helicopterStart", "Helicopter (Start)"),
	HELICOPTER_FINISH("helicopterFinish", "Helicopter (Final Submission)"),
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

	// For Thymeleaf:
	public String getName() {
		return name();
	}

	public static Event forTemplate(String template) throws NoSuchEventException {
		return Stream.of(Event.values())
			.filter(event -> event.templateName.equals(template))
			.findAny()
			.orElseThrow(() -> {
				String knownEvents = Stream.of(Event.values())
					.map(Event::getTemplateName)
					.collect(Collectors.joining(", "));
				return new NoSuchEventException(
					"'%1$s' is not a recognized Science Olympiad event.  Must be one of %2$s",
					template, knownEvents);
			});
	}
}
