package org.deltava.dao.http;

import org.deltava.dao.DAOException;
import org.deltava.dao.http.DAO.Compression;

import junit.framework.TestCase;

public class TestGetURL extends TestCase {

	@SuppressWarnings("static-method")
	public void testURL() throws DAOException {
		GetURL dao = new GetURL("https://dev.deltava.org", null);
		byte[] data = dao.load();
		assertNotNull(data);
		assertTrue(data.length > 0);
	}
	
	@SuppressWarnings("static-method")
	public void testConnectTimeout() {
		try {
			GetURL dao = new GetURL("https://localhost", null);
			dao.setConnectTimeout(250);
			byte[] data = dao.load();
			assertNull(data);
			fail("Timeout expected");
		} catch (DAOException de) {
			// empty
		}
	}
	
	@SuppressWarnings("static-method")
	public void testGZIPEncoding() throws DAOException {
		GetURL dao = new GetURL("https://dev.deltava.org", null);
		dao.setCompression(Compression.GZIP);
		byte[] data = dao.load();
		assertNotNull(data);
		assertTrue(data.length > 0);
		assertEquals(Compression.GZIP, dao.getCompression());
	}
	
	@SuppressWarnings("static-method")
	public void testBrotliEncoding() throws DAOException {
		GetURL dao = new GetURL("https://dev.deltava.org", null);
		dao.setCompression(Compression.BROTLI);
		byte[] data = dao.load();
		assertNotNull(data);
		assertTrue(data.length > 0);
		assertEquals(Compression.BROTLI, dao.getCompression());
	}
}