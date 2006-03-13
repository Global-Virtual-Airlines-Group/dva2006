package org.deltava.util.http;

import java.net.*;

import junit.framework.TestCase;

public class TestHttpTimeoutConnection extends TestCase {
	
	private URLConnection _con;

	protected void tearDown() throws Exception {
		_con = null;
		super.tearDown();
	}

	public void testValidURL() throws Exception {
		URL url = new URL("http", "www.deltava.org", 80, "/", new HttpTimeoutHandler(1000));
		assertNotNull(url);
		_con = url.openConnection();
		assertNotNull(_con);
		assertTrue(_con instanceof HttpTimeoutURLConnection);
		_con.connect();
	}
	
	public void testTimeoutURL() throws Exception {
		URL url = new URL("http", "www.deltava.org", 81, "/", new HttpTimeoutHandler(200));
		assertNotNull(url);
		_con = url.openConnection();
		assertNotNull(_con);
		assertTrue(_con instanceof HttpTimeoutURLConnection);
		try {
			_con.connect();
			fail("ConnectException expected");
		} catch (SocketException se) {
			
		}
	}
}