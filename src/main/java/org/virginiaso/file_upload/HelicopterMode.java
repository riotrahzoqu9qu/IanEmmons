package org.virginiaso.file_upload;

enum HelicopterMode {
	TWO_HELICOPTERS_TWO_STUDENTS("Two helicopters, two students, two videos"),
	TWO_HELICOPTERS_ONE_STUDENT("Two helicopters, one student, two videos"),
	ONE_HELICOPTER_ONE_STUDENT_TWO_VIDEOS("One helicopter, one student, two videos"),
	ONE_HELICOPTER_ONE_STUDENT_ONE_VIDEO("One helicopter, one student, one video");

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
