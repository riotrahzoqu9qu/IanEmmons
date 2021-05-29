package org.virginiaso.file_upload.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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
		String tmpProjectName = DEFAULT_PROJ_NAME;
		String tmpProjectVersion = DEFAULT_PROJ_VERSION;
		try (
			InputStream is = getRsrcAsStream(RSRC_NAME);
			Reader rdr = new InputStreamReader(is, StandardCharsets.UTF_8);
		) {
			Properties prop = new Properties();
			prop.load(rdr);
			tmpProjectName = prop.getProperty(PROJ_NAME_PROP, DEFAULT_PROJ_NAME);
			tmpProjectVersion = prop.getProperty(PROJ_VERSION_PROP, DEFAULT_PROJ_VERSION);
		} catch (IOException ex) {
			LOG.error("Unable to load project info:", ex);
		}
		PROJ_NAME = tmpProjectName;
		PROJ_VERSION = tmpProjectVersion;
	}

	private static InputStream getRsrcAsStream(String rsrc) throws FileNotFoundException {
		ClassLoader cl = ProjectInfo.class.getClassLoader();
		InputStream is = cl.getResourceAsStream(rsrc);
		if (is == null) {
			throw new FileNotFoundException(
				String.format("Unable to find resource '%1$s'", rsrc));
		}
		return is;
	}

	public static String getProjName() {
		return PROJ_NAME;
	}

	public static String getProjVersion() {
		return PROJ_VERSION;
	}
}
