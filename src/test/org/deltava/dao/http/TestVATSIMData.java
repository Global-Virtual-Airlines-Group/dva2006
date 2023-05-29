package org.deltava.dao.http;

import java.io.File;

import org.deltava.beans.servinfo.*;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestVATSIMData extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		
		SystemData.init();
		assertNotNull(SystemData.get("online.vatsim.validation_url"));
		assertNotNull(SystemData.get("security.key.vatsim"));
	}
	
	public void testValidID() throws DAOException {
		GetVATSIMData dao = new GetVATSIMData();
		Certificate c = dao.getInfo("837789");
		assertNotNull(c);
		assertTrue(c.isActive());
	}
	
	public void testATOCert() {
		
		PilotRating pr = new PilotRating(837789, "P1");
		pr.setInstructorID(931991);
		
		try {
			SetVATSIMData atodao = new SetVATSIMData();
			atodao.addRating(pr);
			fail("Failure expected");
		} catch (DAOException de) {
			// empty
		}
	}
}