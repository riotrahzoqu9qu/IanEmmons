package org.virginiaso.file_upload;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.virginiaso.file_upload.util.StringUtil;
import org.virginiaso.file_upload.util.ValidationException;

public enum Event {
	DETECTOR_DESIGN("detectorDesign", "Detector Design"),
	DIGITAL_STRUCTURES("digitalStructures", "Digital Structures"),
	HELICOPTER_START("helicopterStart", "Helicopter (Start)"),
	HELICOPTER_FINISH("helicopterFinish", "Helicopter (Final Submission)"),
	MISCELLANEOUS("miscellaneous", "Miscellaneous"),
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

	public static Event forTemplate(String template) {
		return Stream.of(Event.values())
			.filter(event -> event.templateName.equals(StringUtil.safeTrim(template)))
			.findAny()
			.orElseThrow(() -> {
				String knownEvents = Stream.of(Event.values())
					.map(Event::getTemplateName)
					.collect(Collectors.joining(", "));
				return new ValidationException(
					"'%1$s' is not a recognized Science Olympiad event.  Must be one of %2$s",
					template, knownEvents);
			});
	}
}
