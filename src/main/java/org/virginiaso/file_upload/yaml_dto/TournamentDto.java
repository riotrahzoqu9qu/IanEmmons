package org.virginiaso.file_upload.yaml_dto;

import java.util.List;
import java.util.Map;

public final class TournamentDto {
	public String name;
	public String date;
	public Map<String, String> teams;
	public List<EventDto> events;
}
