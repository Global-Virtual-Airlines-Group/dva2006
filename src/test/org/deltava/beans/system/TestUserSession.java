package org.deltava.beans.system;

import java.util.Date;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestUserSession extends AbstractBeanTestCase {

	private UserSession _s;

	public static Test suite() {
		return new CoverageDecorator(TestUserSession.class, new Class[] { UserSession.class });
	}

	protected void setUp() throws Exception {
		super.setUp();
		_s = new UserSession("ABCD");
		setBean(_s);
	}

	protected void tearDown() throws Exception {
		_s = null;
		super.tearDown();
	}

	public void testProperties() {
		assertEquals("ABCD", _s.getSessionID());
		assertEquals(_s.getSessionID().hashCode(), _s.hashCode());
		checkProperty("startTime", new Date());
		checkProperty("endTime", new Date());
		checkProperty("remoteAddr", "127.0.0.1");
		checkProperty("remoteHost", "localhost");
		checkProperty("pilotID", new Integer(1234));
		_s.setPilotName("John", "Smith");
		assertEquals("John Smith", _s.getPilotName());
	}
	
	public void testComparator() throws Exception{
		UserSession s2 = new UserSession("BCDE");
		long now = System.currentTimeMillis();
		_s.setStartTime(new Date(now));
		s2.setStartTime(new Date(now + 1));
		assertTrue(_s.compareTo(s2) < 0);
	}
	
	public void testLength() throws Exception {
		long now = System.currentTimeMillis();
		_s.setStartTime(new Date(now));
		
		Thread.sleep(50);
		assertTrue(_s.getLength() > 50);
		
		long now2 = System.currentTimeMillis();
		_s.setEndTime(new Date(now2));
		
		Thread.sleep(50);
		assertEquals((now2 - now), _s.getLength());
		
		Thread.sleep(50);
		assertEquals((now2 - now), _s.getLength());
	}

	public void testValidation() {
		long now = System.currentTimeMillis();
		_s.setStartTime(new Date(now));
		_s.setEndTime(null);
		validateInput("endTime", new Date(now - 1), IllegalArgumentException.class);
		validateInput("pilotID", new Integer(-1), IllegalArgumentException.class);
		try {
			_s.setStartTime(null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException iae) {
		}
		
		try {
			_s.setEndTime(new Date(now -1));
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException iae) {
		}
	}
}