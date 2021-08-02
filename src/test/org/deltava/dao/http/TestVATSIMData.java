package org.deltava.dao.http;

import org.apache.log4j.*;

import org.deltava.beans.servinfo.*;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestVATSIMData extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
		
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
	
	public void testGetATOData() throws DAOException {

		GetATOData atodao = new GetATOData();
		atodao.setReadTimeout(30000);
		assertFalse(atodao.getInstructors().isEmpty());
		atodao.reset();
		assertFalse(atodao.getCertificates().isEmpty());
	}
}