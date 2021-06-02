package org.virginiaso.file_upload;

public enum NotesUploadMode {
	TWO_STUDENTS_SAME_NOTES(
		"My partner and I use the same notes &mdash; only this upload is necessary"),
	TWO_STUDENT_DIFFERENT_NOTES(
		"My partner and I will each upload our own notes"),
	ONE_STUDENT(
		"I don&rsquo;t have a partner");

	private final String label;

	private NotesUploadMode(String label) {
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
