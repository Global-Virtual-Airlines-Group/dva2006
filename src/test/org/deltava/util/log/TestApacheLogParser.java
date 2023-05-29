// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util.log;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.logging.log4j.*;
import org.hansel.CoverageDecorator;

import org.deltava.beans.stats.HTTPStatistics;

public class TestApacheLogParser extends TestCase {

	private static final Logger log = LogManager.getLogger(TestApacheLogParser.class);

	private LogParser _parser;
	private File _log;

	public static Test suite() {
		return new CoverageDecorator(TestApacheLogParser.class, new Class[] { ApacheLogParser.class });
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		_parser = new ApacheLogParser();
		_log = new File("data/httpd-access.dva.log.1124409600");
		assertTrue(_log.exists());
	}

	public void testParser() {
		HTTPStatistics stats = _parser.parseLog(_log);
		assertNotNull(stats);
		assertNotNull(stats.getDate());
		assertEquals(1124409600000L, stats.getDate().toEpochMilli());
		assertEquals(18, stats.getRequests());
		assertEquals(3, stats.getHomePageHits());
		assertEquals(7854, stats.getExecutionTime());
		assertEquals(7030, stats.getBackEndTime());
		assertEquals(168505, stats.getBandwidth());
	}

	public void testNoFile() {
		File f = new File("data/dummy.txt");
		assertFalse(f.exists());
		HTTPStatistics stats = _parser.parseLog(f);
		assertNull(stats);
	}

	public void testCrappyData() {
		_log = new File("data/httpd-access.crap.log.1124409600");
		assertTrue(_log.exists());
		HTTPStatistics stats = _parser.parseLog(_log);
		assertNotNull(stats);
		assertNotNull(stats.getDate());
		assertEquals(1124409600000L, stats.getDate().toEpochMilli());
		log.debug("Date = " + stats.getDate());
	}
}