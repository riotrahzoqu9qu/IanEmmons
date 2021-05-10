package org.virginiaso.file_upload;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Column {
	EVENT(false),
	ID(false),
	VA_DATE_TIME(false),
	DIVISION(false),
	TEAM_NUMBER(false),
	SCHOOL_NAME(false),
	TEAM_NAME(false),
	STUDENT_NAMES(false),
	NOTES(false),
	HELICOPTER_MODE(false),	// Helicopter only
	FLIGHT_DURATION(false),	// Helicopter only
	PASS_CODE(false),			// Helicopter only
	FILE_NAME_0(true),
	FILE_NAME_1(true),
	FILE_NAME_2(true),
	FILE_NAME_3(true),
	FILE_NAME_4(true),
	FILE_NAME_5(true),
	FILE_NAME_6(true),
	FILE_NAME_7(true),
	FILE_NAME_8(true),
	FILE_NAME_9(true),
	UTC_TIME_STAMP(false);

	private final boolean isFileColumn;

	private Column(boolean isFileColumn) {
		this.isFileColumn = isFileColumn;
	}

	public static List<Column> fileColumns() {
		return Stream.of(Column.values())
			.filter(column -> column.isFileColumn)
			.collect(Collectors.toUnmodifiableList());
	}
}
