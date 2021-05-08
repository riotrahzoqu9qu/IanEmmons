package org.virginiaso.file_upload.yaml_dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
	justification = "SnakeYAML initializes DTO object fields via reflection")
public final class TimeIntervalDto {
	public String from;
	public String to;
}
