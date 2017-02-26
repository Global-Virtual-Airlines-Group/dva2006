// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import org.apache.log4j.*;

import org.deltava.beans.Pilot;
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
	
	public void testAccents() throws DAOException {
		GetVATSIMData dao = new GetVATSIMData();
		Certificate c = dao.getInfo("1067774");
		assertNotNull(c);
		Certificate c2 = new Certificate(1067774);
		assertNotNull(c2);
		c2.setFirstName("Gilson");
		c2.setLastName("GuimarÃes");

		Pilot p = new Pilot("Gilson", "GUIMARÃES");
		assertNotNull(p);
		assertTrue(c2.comapreName(p));
		assertTrue(c.comapreName(p));
		
		Pilot p2 = new Pilot("Gilson", "GUIMARaES");
		assertTrue(c.comapreName(p2));
	}
	
	public void testATOCert() throws DAOException {
		
		PilotRating pr = new PilotRating(837789, "P1");
		pr.setInstructorID(931991);
		
		SetVATSIMData atodao = new SetVATSIMData();
		atodao.addRating(pr);
	}
}