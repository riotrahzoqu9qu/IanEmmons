package org.virginiaso.file_upload.yaml_dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
	justification = "SnakeYAML initializes DTO object fields via reflection")
public final class EventDto {
	public String name;

	/*
	 * The following member names don't follow convention because
	 * they need to match the case used in the configuration file.
	 */

	@SuppressWarnings("checkstyle:MemberName")
	public TimeIntervalDto A;

	@SuppressWarnings("checkstyle:MemberName")
	public TimeIntervalDto B;

	@SuppressWarnings("checkstyle:MemberName")
	public TimeIntervalDto C;

	@SuppressWarnings("checkstyle:MemberName")
	public TimeIntervalDto BC;
}
