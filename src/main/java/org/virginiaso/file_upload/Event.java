package org.virginiaso.file_upload;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.virginiaso.file_upload.util.StringUtil;
import org.virginiaso.file_upload.util.ValidationException;

public enum Event {
	ANATOMY(true, "anatomy", "Anatomy & Physiology"),
	CHEM_LAB(true, "chemLab", "Chemistry Lab"),
	CRIME_BUSTERS(true, "crimeBusters", "Crime Busters"),
	DESIGNER_GENES(true, "designerGenes", "Designer Genes"),
	DISEASE_DETECTIVES(true, "diseaseDetectives", "Disease Detectives"),
	FOOD_SCIENCE(true, "foodScience", "Food Science"),
	FORENSICS(true, "forensics", "Forensics"),
	HEREDITY(true, "heredity", "Heredity"),
	METEOROLOGY(true, "meteorology", "Meteorology"),
	PROTEIN_MODELING(true, "proteinModeling", "Protein Modeling"),
	REACH_FOR_THE_STARS(true, "reachForTheStars", "Reach for the Stars"),
	WATER_QUALITY(true, "waterQuality", "Water Quality"),

	DETECTOR_DESIGN(false, "detectorDesign", "Detector Design"),
	DIGITAL_STRUCTURES(false, "digitalStructures", "Digital Structures"),
	HELICOPTER_START(false, "helicopterStart", "Helicopter (Start)"),
	HELICOPTER_FINISH(false, "helicopterFinish", "Helicopter (Final Submission)"),
	MISCELLANEOUS(false, "miscellaneous", "Miscellaneous"),
	VEHICLE_DESIGN(false, "vehicleDesign", "Vehicle Design"),
	WICI(false, "wici", "Write It/CAD It (WICI)");

	private static final String NOTES_TEMPLATE_NAME = "notes";

	private final boolean isNotesUpload;
	private final String uri;
	private final String label;

	private Event(boolean isNotesUpload, String uri, String label) {
		this.isNotesUpload = isNotesUpload;
		this.uri = Objects.requireNonNull(uri, "uri");
		this.label = Objects.requireNonNull(label, "label");
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
