package org.virginiaso.file_upload;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virginiaso.file_upload.util.StringUtil;
import org.virginiaso.file_upload.util.ValidationException;

public enum Event {
	ANATOMY("anatomy", "Anatomy & Physiology", true, "BC"),
	CHEM_LAB("chemLab", "Chemistry Lab", true, "C"),
	CRIME_BUSTERS("crimeBusters", "Crime Busters", true, "B"),
	DESIGNER_GENES("designerGenes", "Designer Genes", true, "C"),
	DISEASE_DETECTIVES("diseaseDetectives", "Disease Detectives", true, "BC"),
	FOOD_SCIENCE("foodScience", "Food Science", true, "B"),
	FORENSICS("forensics", "Forensics", true, "C"),
	HEREDITY("heredity", "Heredity", true, "B"),
	METEOROLOGY("meteorology", "Meteorology", true, "B"),
	PROTEIN_MODELING("proteinModeling", "Protein Modeling", true, "C"),
	REACH_FOR_THE_STARS("reachForTheStars", "Reach for the Stars", true, "B"),
	WATER_QUALITY("waterQuality", "Water Quality", true, "BC"),

	DETECTOR_DESIGN("detectorDesign", "Detector Design", false, "C"),
	DIGITAL_STRUCTURES("digitalStructures", "Digital Structures", false, "BC"),
	HELICOPTER_START("helicopterStart", "Helicopter (Start)", false, "BC"),
	HELICOPTER_FINISH("helicopterFinish", "Helicopter (Final Submission)", false, "BC"),
	MISCELLANEOUS("miscellaneous", "Miscellaneous", false, "ABC"),
	VEHICLE_DESIGN("vehicleDesign", "Vehicle Design", false, "BC"),
	WICI("wici", "Write It/CAD It (WICI)", false, "BC"),

	BUILD_A_BARGE("barge", "Build-A-Barge", false, "A"),
	CHOPPER_CHALLENGE("chopper", "Chopper Challenge", false, "A"),
	MISSION_POSSIBLE("missionPossible", "Mission Possible", false, "A"),
	WIND_POWER("windPower", "Wind Power", false, "A"),
	WRIGHT_STUFF("wrightStuff", "Wright Stuff", false, "A");

	private static final String NOTES_TEMPLATE_NAME = "notes";
	private static final Logger LOG = LoggerFactory.getLogger(Event.class);

	private final String uri;
	private final String label;
	private final boolean isNotesUpload;
	private final EnumSet<Division> divisions;

	private Event(String uri, String label, boolean isNotesUpload, String divLetters) {
		this.uri = Objects.requireNonNull(uri, "uri");
		this.label = Objects.requireNonNull(label, "label");
		this.isNotesUpload = isNotesUpload;
		divisions = stringToEnumSet(label, divLetters);
	}

	private static EnumSet<Division> stringToEnumSet(String eventLbl, String divLetters) {
		if (divLetters == null) {
			LOG.error("{} has a null division specification", eventLbl);
			System.exit(-1);
		}
		EnumSet<Division> result = EnumSet.noneOf(Division.class);
		try {
			for (int i = 0; i < divLetters.length(); ++i) {
				String divLetter = divLetters.substring(i, i + 1);
				result.add(Division.valueOf(divLetter));
			}
		} catch (IllegalArgumentException ex) {
			LOG.error("{} has a bad division specification '{}'", eventLbl, divLetters);
			System.exit(-1);
		}
		if (result.isEmpty()) {
			LOG.error("{} has no division specified", eventLbl);
			System.exit(-1);
		}
		return result;
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

	public Set<Division> getDivisions() {
		return Collections.unmodifiableSet(divisions);
	}

	// For Thymeleaf:
	public String getName() {
		return name();
	}

	public boolean isOfferedIn(Division division) {
		return divisions.contains(division);
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

	public static List<Event> getAllEvents() {
		return getEvents(event -> true);
	}

	public static List<Event> getNotesUploadEvents() {
		return getEvents(Event::isNotesUpload);
	}

	private static List<Event> getEvents(Predicate<Event> predicate) {
		return List.of(Event.values()).stream()
			.filter(predicate)
			.sorted(Comparator.comparing(event -> event.getLabel()))
			.collect(Collectors.toUnmodifiableList());
	}

	public String getDivisionsAsString() {
		if (divisions.size() < 3) {
			return divisions.stream()
				.map(Enum::toString)
				.collect(Collectors.joining(" or "));
		} else {
			int i = 0;
			StringBuilder buffer = new StringBuilder();
			for (Division division : divisions) {
				buffer.append(division.toString());
				if (i < divisions.size() - 2) {
					buffer.append(", ");
				} else if (i < divisions.size() - 1) {
					buffer.append(", or ");
				}
				++i;
			}
			return buffer.toString();
		}
	}
}
