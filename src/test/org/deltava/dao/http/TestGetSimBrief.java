package org.deltava.dao.http;

import junit.framework.TestCase;

import org.deltava.beans.simbrief.SimBriefParser;

import org.deltava.dao.http.GetSimBrief.SimBriefException; 

public class TestGetSimBrief extends TestCase {

	private static final String USER = "248733";
	
	private GetSimBrief sbdao;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sbdao = new GetSimBrief();
		sbdao.setConnectTimeout(2500);
		sbdao.setReadTimeout(4500);
		sbdao.setCompression(Compression.GZIP);
		sbdao.setReturnErrorStream(true);
	}
	
	public void testLatestPlan() throws Exception {
		String xml = sbdao.refresh("LukeKolin", null);
		assertNotNull(xml);
	}
	
	public void testInvalidURL() throws Exception {
		try {
			sbdao.refresh("5619", null);
		} catch (SimBriefException sbe) {
			assertNotNull(sbe.getMessage());
			String errorMsg = SimBriefParser.parseError(sbe.getMessage());
			assertNotNull(errorMsg);
		}
	}
	
	public void testRefreshError() throws Exception {
		try {
			sbdao.refresh(USER, "0x19f4cd");
			fail("SimBriefException expected");
		} catch (SimBriefException hde) {
			assertNotNull(hde.getMessage());
			String errorMsg = SimBriefParser.parseError(hde.getMessage());
			assertNotNull(errorMsg);
		}
	}
}