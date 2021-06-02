package org.virginiaso.file_upload;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.virginiaso.file_upload.util.StringUtil;
import org.virginiaso.file_upload.util.ValidationException;

public enum Event {
	ANATOMY("anatomy", "Anatomy & Physiology", true),
	CHEM_LAB("chemLab", "Chemistry Lab", true),
	CRIME_BUSTERS("crimeBusters", "Crime Busters", true),
	DESIGNER_GENES("designerGenes", "Designer Genes", true),
	DISEASE_DETECTIVES("diseaseDetectives", "Disease Detectives", true),
	FOOD_SCIENCE("foodScience", "Food Science", true),
	FORENSICS("forensics", "Forensics", true),
	HEREDITY("heredity", "Heredity", true),
	METEOROLOGY("meteorology", "Meteorology", true),
	PROTEIN_MODELING("proteinModeling", "Protein Modeling", true),
	REACH_FOR_THE_STARS("reachForTheStars", "Reach for the Stars", true),
	WATER_QUALITY("waterQuality", "Water Quality", true),

	DETECTOR_DESIGN("detectorDesign", "Detector Design", false),
	DIGITAL_STRUCTURES("digitalStructures", "Digital Structures", false),
	HELICOPTER_START("helicopterStart", "Helicopter (Start)", false),
	HELICOPTER_FINISH("helicopterFinish", "Helicopter (Final Submission)", false),
	MISCELLANEOUS("miscellaneous", "Miscellaneous", false),
	VEHICLE_DESIGN("vehicleDesign", "Vehicle Design", false),
	WICI("wici", "Write It/CAD It (WICI)", false);

	private static final String NOTES_TEMPLATE_NAME = "notes";

	private final String uri;
	private final String label;
	private final boolean isNotesUpload;

	private Event(String uri, String label, boolean isNotesUpload) {
		this.uri = Objects.requireNonNull(uri, "uri");
		this.label = Objects.requireNonNull(label, "label");
		this.isNotesUpload = isNotesUpload;
	}

	public String getUri() {
		return uri;
	}

	public String getTemplateName() {
		return isNotesUpload ? NOTES_TEMPLATE_NAME : uri;
	}

	public String getLabel() {
		return label;
	}

	public boolean isNotesUpload() {
		return isNotesUpload;
	}

	// For Thymeleaf:
	public String getName() {
		return name();
	}

	public static Event forUri(String uri) {
		return Stream.of(Event.values())
			.filter(event -> event.uri.equals(StringUtil.safeTrim(uri)))
			.findAny()
			.orElseThrow(() -> {
				String knownEvents = Stream.of(Event.values())
					.map(Event::getUri)
					.collect(Collectors.joining(", "));
				return new ValidationException(
					"'%1$s' is not a recognized Science Olympiad event.  Must be one of %2$s",
					uri, knownEvents);
			});
	}
}
