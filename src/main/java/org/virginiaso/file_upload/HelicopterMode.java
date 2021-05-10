package org.virginiaso.file_upload;

public enum HelicopterMode {
	TWO_HELICOPTERS_TWO_STUDENTS_TWO_VIDEOS("Two helicopters, two students, two videos"),
	TWO_HELICOPTERS_ONE_STUDENT_TWO_VIDEOS("Two helicopters, one student, two videos"),
	ONE_HELICOPTER_ONE_STUDENT_TWO_VIDEOS("One helicopter, one student, two videos");

	private final String label;

	private HelicopterMode(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	// For Thymeleaf:
	public String getName() {
		return name();
	}
}
