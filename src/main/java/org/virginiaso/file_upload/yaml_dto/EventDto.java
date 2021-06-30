package org.virginiaso.file_upload.yaml_dto;

public final class EventDto {
	public String name;

	/*
	 * The following member names don't follow convention because
	 * they need to match the case used in the configuration file.
	 */

	public TimeIntervalDto A;
	public TimeIntervalDto B;
	public TimeIntervalDto C;
	public TimeIntervalDto BC;
}
