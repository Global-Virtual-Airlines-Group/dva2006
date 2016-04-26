// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import org.apache.log4j.PropertyConfigurator;

import org.deltava.beans.fb.*;
import org.deltava.dao.http.*;

import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestFacebookDAO extends TestCase {

	private static final String TOKEN = "93603160577|02faa99c85a2e78c9eaf8dff-521227914|bQRcIuPZcvp0fhqq6wj3Iw1VVk1";
	private static final String TOKEN2 = "205671896115290|c3f36cd31f4aa4b37e3a2755.1-100000044952035|ReicmZp8Y_F-pbUuGaoppSkNPBE";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
		SystemData.init();
	}
	
	
	public void testGetInfo() throws Exception {
		
		GetFacebookData fbdao = new GetFacebookData();
		fbdao.setToken(TOKEN);
		ProfileInfo info = fbdao.getUserInfo();
		assertNotNull(info);
		assertEquals("Luke", info.getFirstName());
		assertEquals("Kolin", info.getLastName());
	}
	
	public void testPostNews() throws Exception {
		
		// Build the news entry
		NewsEntry nws = new NewsEntry("I am able to successfully post to Facebook", "http://www.deltava.org/");
		nws.setLinkDescription("Golgotha Unit Test Link Description");
		nws.setLinkCaption("DVA ACARS");
		nws.setImageURL(SystemData.get("users.facebook.img"));
		
		SetFacebookData fbdao = new SetFacebookData();
		fbdao.setToken(TOKEN);
		fbdao.write(nws);
		assertNotNull(nws.getID());
	}
	
	public void testBadToken() throws Exception {
		
		GetFacebookData fbdao = new GetFacebookData();
		fbdao.setToken(TOKEN2);
		ProfileInfo info = fbdao.getUserInfo();
		assertNotNull(info);		
		
	}
}