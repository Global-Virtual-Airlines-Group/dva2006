// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import org.apache.log4j.PropertyConfigurator;

import org.deltava.beans.fb.NewsEntry;
import org.deltava.dao.http.SetFacebookData;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class TestFacebookAppWallPost extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
		SystemData.init();
	}

	@SuppressWarnings("static-method")
	public void testPostAppWall() throws Exception {

		// Build the news entry
		NewsEntry nws = new NewsEntry("I am able to successfully post to Facebook"); //, "http://www.deltava.org/");
		//nws.setLinkDescription("Golgotha Unit Test Link Description");
		//nws.setLinkCaption("DVA ACARS");
		nws.setImageURL(SystemData.get("users.facebook.img"));

		SetFacebookData fbdao = new SetFacebookData();
		fbdao.setAppID(SystemData.get("users.facebook.pageID"));
		fbdao.setToken(SystemData.get("users.facebook.pageToken"));
		fbdao.writeApp(nws);
		assertNotNull(nws.getID());
	}
}