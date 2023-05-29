package org.deltava.util.ftp;

import junit.framework.TestCase;

import java.io.File;

import org.deltava.util.system.SystemData;

import com.enterprisedt.net.ftp.*;

public class TestFTPDownload extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		SystemData.init("org.deltava.util.system.XMLSystemDataLoader", true);
	}

	@SuppressWarnings("static-method")
	public void testDownload() throws Exception {
		assertNotNull(SystemData.get("schedule.innovata.download.host"));
		assertNotNull(SystemData.get("schedule.innovata.download.user"));
		assertNotNull(SystemData.get("schedule.innovata.download.pwd"));
		assertNotNull(SystemData.get("schedule.innovata.download.file"));
		
		// Create the session and connect
		FTPClient ftp = new FTPClient();
		ftp.setRemoteHost(SystemData.get("schedule.innovata.download.host"));
		ftp.connect();
		assertTrue(ftp.connected());
		
		// Log in
		try {
			ftp.login(SystemData.get("schedule.innovata.download.user"), SystemData.get("schedule.innovata.download.pwd"));
			byte[] buffer = ftp.get(SystemData.get("schedule.innovata.download.file"));
			assertNotNull(buffer);
			ftp.quit();
		} catch (FTPException fe) {
			fail(fe.getMessage());
		}
	}
}