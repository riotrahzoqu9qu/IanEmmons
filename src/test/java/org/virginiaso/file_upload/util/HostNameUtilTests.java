package org.virginiaso.file_upload.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class HostNameUtilTests {
	private static final String OP_SYS = System.getProperty("os.name").toLowerCase();
	private static final boolean IS_WIN = OP_SYS.contains("win");
	private static final boolean IS_MAC = OP_SYS.contains("mac");

	@Test
	public void testGetHostName() {
		String hostName = HostNameUtil.getHostName();
		System.out.format("Host name = '%1$s'%n", hostName);
		assertNotEquals(null, hostName);
	}

	@Test
	public void testGetHostByInetAddress() {
		String hostName = HostNameUtil.getHostByInetAddress();
		System.out.format("Host name by InetAddress = '%1$s'%n", hostName);
		assertNotEquals(null, hostName);
	}

	@Test
	public void testGetHostByEnvVar() {
		String hostName = HostNameUtil.getHostByEnvVar(IS_WIN
			? HostNameUtil.WINDOWS_ENV_VAR
			: HostNameUtil.LINUX_ENV_VAR);
		System.out.format("Host name by env var = '%1$s'%n", hostName);
		if (IS_MAC) {
			assertEquals(null, hostName);
		} else {
			assertNotEquals(null, hostName);
		}
	}

	@Test
	public void testGetHostBySystemCommand() {
		String hostName = HostNameUtil.getHostBySystemCommand();
		System.out.format("Host name by system command = '%1$s'%n", hostName);
		assertNotEquals(null, hostName);
	}

	@Test
	public void testGetHostByEtcHostname() {
		String hostName = HostNameUtil.getHostByEtcHostname();
		System.out.format("Host name by /etc/hostname = '%1$s'%n", hostName);
		if (IS_MAC || IS_WIN) {
			assertEquals(null, hostName);
		} else {
			assertNotEquals(null, hostName);
		}
	}
}
