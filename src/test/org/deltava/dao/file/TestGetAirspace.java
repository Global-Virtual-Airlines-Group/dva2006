// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.navdata.Airspace;

import org.deltava.dao.DAOException;

public class TestGetAirspace extends TestCase {
	
	private Logger log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(GetAirspaceDefinition.class);
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadAirspace() throws DAOException {
		
		File f = new File("data/allusa.v17.03-30.1.txt");
		assertTrue(f.exists());
		try (FileInputStream fs = new FileInputStream(f)) {
			GetAirspaceDefinition adao = new GetAirspaceDefinition(fs);
			Collection<Airspace> results = adao.load();
			assertFalse(results.isEmpty());
			results.forEach(a -> {
				if ((a.getID().length() > 63) || (a.getName().length() > 95))
					log.warn(a.getID() + " " + a.getName());
			});
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}