// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.logging.log4j.LogManager;

import org.deltava.beans.finance.AWSCostEntry;

import org.deltava.util.ConfigLoader;
import org.deltava.util.system.SystemData;

public class TestGetAWSCosts extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		SystemData.init();
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	@SuppressWarnings("static-method")
	public void testJSONParse() throws Exception {
		
		try (InputStream is = ConfigLoader.getStream("/data/aws_costs.json")) {
			GetAWSCosts dao = new GetAWSCosts();
			dao.setStream(is);
			
			Collection<AWSCostEntry> results = dao.getCosts(3);
			assertNotNull(results);
			assertFalse(results.isEmpty());
		}
	}
}