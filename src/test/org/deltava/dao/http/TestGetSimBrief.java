package org.deltava.dao.http;

import org.deltava.beans.simbrief.SimBriefParser;

import junit.framework.TestCase;

public class TestGetSimBrief extends TestCase {

	private static final String USER = "248733";
	
	@SuppressWarnings("static-method")
	public void testRefreshError() throws Exception {
		
		GetSimBrief sbdao = new GetSimBrief();
		sbdao.setConnectTimeout(2500);
		sbdao.setReadTimeout(4500);
		sbdao.setCompression(Compression.BROTLI, Compression.GZIP);
		sbdao.setReturnErrorStream(true);
		
		try {
			sbdao.refresh(USER, "0x19f4cd");
			fail("SimBriefException expected");
		} catch (HTTPDAOException hde) {
			assertTrue(hde instanceof GetSimBrief.SimBriefException);
			String errorMsg = SimBriefParser.parseError(hde.getMessage());
			assertNotNull(errorMsg);
		}
	}
}