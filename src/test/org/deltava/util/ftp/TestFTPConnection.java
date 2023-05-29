package org.deltava.util.ftp;

import junit.framework.TestCase;

import java.io.File;

import com.enterprisedt.net.ftp.*;

import org.deltava.util.system.SystemData;

public class TestFTPConnection extends TestCase {
	
	private FTPConnection _con;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		SystemData.init("org.deltava.util.system.XMLSystemDataLoader", true);
		assertNotNull(SystemData.get("schedule.innovata.download.host"));
		assertNotNull(SystemData.get("schedule.innovata.download.user"));
		assertNotNull(SystemData.get("schedule.innovata.download.pwd"));
		assertNotNull(SystemData.get("schedule.innovata.download.file"));
	}

	@Override
	protected void tearDown() throws Exception {
		_con.close();
		_con = null;
		super.tearDown();
	}
	
	public void testConnection() throws FTPClientException {
		_con = new FTPConnection(SystemData.get("schedule.innovata.download.host"));
		assertNotNull(_con);
		assertNotNull(_con.getClient());
		_con.connect(SystemData.get("schedule.innovata.download.user"), SystemData.get("schedule.innovata.download.pwd"));
		assertTrue(_con.isConnected());
		assertEquals(FTPTransferType.BINARY, _con.getClient().getType());
	}
	
	public void testFileFunctions() throws FTPClientException {
		_con = new FTPConnection(SystemData.get("schedule.innovata.download.host"));
		assertNotNull(_con);
		_con.connect(SystemData.get("schedule.innovata.download.user"), SystemData.get("schedule.innovata.download.pwd"));
		assertTrue(_con.isConnected());
		assertTrue(_con.hasFile("", SystemData.get("schedule.innovata.download.file")));
		assertFalse(_con.hasFile("", "foo.bar"));
		assertNotNull(_con.getTimestamp("", SystemData.get("schedule.innovata.download.file")));
		assertNull(_con.getTimestamp("", "foo.bar"));
	}
}