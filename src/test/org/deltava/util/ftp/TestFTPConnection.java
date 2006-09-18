package org.deltava.util.ftp;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import com.enterprisedt.net.ftp.*;

import org.deltava.util.system.SystemData;

public class TestFTPConnection extends TestCase {
	
	private FTPConnection _con;

	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");
		SystemData.init("org.deltava.util.system.XMLSystemDataLoader", true);
		assertNotNull(SystemData.get("schedule.innovata.download.host"));
		assertNotNull(SystemData.get("schedule.innovata.download.user"));
		assertNotNull(SystemData.get("schedule.innovata.download.pwd"));
		assertNotNull(SystemData.get("schedule.innovata.download.file"));
	}

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