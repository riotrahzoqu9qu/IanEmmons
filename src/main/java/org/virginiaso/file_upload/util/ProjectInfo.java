package org.virginiaso.file_upload.util;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProjectInfo {
	private static final String RSRC_NAME = "project-info.properties";
	private static final String PROJ_NAME_PROP = "projectName";
	private static final String PROJ_VERSION_PROP = "projectVersion";
	private static final String DEFAULT_PROJ_NAME = "Unknown project name";
	private static final String DEFAULT_PROJ_VERSION = "Unknown project version";
	private static final Logger LOG = LoggerFactory.getLogger(ProjectInfo.class);

	private static final String PROJ_NAME;
	private static final String PROJ_VERSION;

	private ProjectInfo() {}	// prevents instantiation

	static {
		var tmpProjectName = DEFAULT_PROJ_NAME;
		var tmpProjectVersion = DEFAULT_PROJ_VERSION;
		try (var rdr = FileUtil.getResourceAsReader(RSRC_NAME)) {
			var prop = new Properties();
			prop.load(rdr);
			tmpProjectName = prop.getProperty(PROJ_NAME_PROP, DEFAULT_PROJ_NAME);
			tmpProjectVersion = prop.getProperty(PROJ_VERSION_PROP, DEFAULT_PROJ_VERSION);
		} catch (MissingResourceException | IOException ex) {
			LOG.error("Unable to load project info:", ex);
		}
		PROJ_NAME = tmpProjectName;
		PROJ_VERSION = tmpProjectVersion;
	}

	public static String getProjName() {
		return PROJ_NAME;
	}

	public static String getProjVersion() {
		return PROJ_VERSION;
	}
}
