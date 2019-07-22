// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

import org.deltava.beans.*;
import org.deltava.beans.stats.GeocodeResult;

import org.deltava.dao.*;
import org.deltava.dao.http.GetGoogleGeocode;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

public class SetLocations extends TestCase {

	private static Logger log;

	private static final String JDBC_URL = "jdbc:mysql://cerberus.gvagroup.org/dva?useUnicode=true&characterEncoding=UTF-8";
	private static final String JDBC2_URL = "jdbc:mysql://polaris.sce.net/dva?useUnicode=true&characterEncoding=UTF-8";

	private Connection _c;
	private Connection _c2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(SetLocations.class);

		SystemData.init();

		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		DriverManager.setLoginTimeout(5);
		_c = DriverManager.getConnection(JDBC_URL, "luke", "luke");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
		
		_c2 = DriverManager.getConnection(JDBC2_URL, "luke", "test");
		assertNotNull(_c2);
		_c2.setAutoCommit(false);
		assertFalse(_c2.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c2.close();
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadLocations() throws Exception {

		// Get locations and Pilots
		GetPilot pdao = new GetPilot(_c2);
		//GetPilot pdao2 = new GetPilot(_c);
		GetPilotBoard pbdao = new GetPilotBoard(_c);
		SetPilot pwdao = new SetPilot(_c);
		Map<Integer, GeoLocation> locs = pbdao.getAll();

		// Loop through the locations
		for (Iterator<Map.Entry<Integer, GeoLocation>> i = locs.entrySet().iterator(); i.hasNext();) {
			Map.Entry<Integer, GeoLocation> e = i.next();
			GeoLocation loc = e.getValue();
			Pilot usr = pdao.get(e.getKey().intValue());
			usr.setLocation(null);

			// Read via the DAO
			if (StringUtils.isEmpty(usr.getLocation())) {
				GeocodeResult gr = null;
				try {
					GetGoogleGeocode gcdao = new GetGoogleGeocode();
					gr = gcdao.getGeoData(loc);
					int distance = loc.distanceTo(gr);
					if ((distance < 30) && (gr != null)) {
						log.warn("Setting " + usr.getName() + " home town to " + gr.getCityState());
						pwdao.setHomeTown(e.getKey().intValue(), gr);
					}
				} catch (Exception ex) {
					log.error("Cannot update " + e.getKey() + " - " + ex.getMessage(), ex);
				}
			}
		}

		// Commit
		_c.commit();
	}
}