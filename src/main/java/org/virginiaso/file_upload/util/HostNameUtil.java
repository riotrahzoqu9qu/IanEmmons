package org.virginiaso.file_upload.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Supplier;

public final class HostNameUtil {
	static final String WINDOWS_ENV_VAR = "COMPUTERNAME";
	static final String LINUX_ENV_VAR = "HOSTNAME";
	private static final List<Supplier<String>> SUPPLIERS = List.of(
		HostNameUtil::getHostByInetAddress,
		() -> getHostByEnvVar(WINDOWS_ENV_VAR),
		() -> getHostByEnvVar(LINUX_ENV_VAR),
		HostNameUtil::getHostBySystemCommand,
		HostNameUtil::getHostByEtcHostname);

	private HostNameUtil() {}	// prevent instantiation

	public static String getHostName() {
		return SUPPLIERS.stream()
			.map(supplier -> supplier.get())
			.filter(string -> string != null)
			.map(String::trim)
			.filter(string -> !string.isEmpty())
			.findFirst()
			.orElse(null);
	}

	static String getHostByInetAddress() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	static String getHostByEnvVar(String envVarName) {
		return System.getenv(envVarName);
	}

	static String getHostBySystemCommand() {
		try {
			var process = new ProcessBuilder("hostname")
				.redirectErrorStream(true)
				.start();
			try (InputStream is = process.getInputStream()) {
				return new String(is.readAllBytes(), FileUtil.CHARSET);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	static String getHostByEtcHostname() {
		var file = new File("/etc/hostname");
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		try {
			var content = Files.readAllBytes(file.toPath());
			return new String(content, FileUtil.CHARSET);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
