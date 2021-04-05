package org.virginiaso.file_upload;

import java.util.Arrays;
import java.util.List;

enum Column {
	EVENT,
	ID,
	VA_DATE,
	VA_TIME,
	DIVISION,
	TEAM_NUMBER,
	SCHOOL_NAME,
	TEAM_NAME,
	STUDENT_NAMES,
	NOTES,
	HELICOPTER_MODE,
	FLIGHT_DURATION,
	UTC_TIME_STAMP,
	FILE_NAME_0,
	FILE_NAME_1,
	FILE_NAME_2,
	FILE_NAME_3,
	FILE_NAME_4,
	FILE_NAME_5,
	FILE_NAME_6,
	FILE_NAME_7,
	FILE_NAME_8,
	FILE_NAME_9;

	public static List<Column> fileColumns() {
		return Arrays.asList(Column.FILE_NAME_0, Column.FILE_NAME_1, Column.FILE_NAME_2,
			Column.FILE_NAME_3, Column.FILE_NAME_4, Column.FILE_NAME_5, Column.FILE_NAME_6,
			Column.FILE_NAME_7, Column.FILE_NAME_8, Column.FILE_NAME_9);
	}
}
