// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.flight.*;

import org.deltava.dao.*;

import org.deltava.util.GeoUtils;
import org.deltava.util.system.SystemData;

public class TestETOPSFull extends TestCase {
	
	private static final String URL = "jdbc:mysql://polaris.sce.net/dva?user=luke&password=test";
	
	private Connection _c;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Class<?> c = Class.forName("com.mysql.jdbc.Driver");
		assertNotNull(c);
		_c = DriverManager.getConnection(URL);
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		
		SystemData.init();
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetAirline aldao = new GetAirline(_c);
		SystemData.add("airlines", aldao.getAll());

		GetAirport apdao = new GetAirport(_c);
		Map<String, Airport> airports = apdao.getAll();
		SystemData.add("airports", airports);
		ETOPSHelper.init(airports.values());
	}
	
	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}

	@SuppressWarnings("static-method")
	public void testAtlantic() throws Exception {
		
		Airport jfk = SystemData.getAirport("KJFK");
		assertNotNull(jfk);
		Airport snn = SystemData.getAirport("EINN");
		assertNotNull(snn);
		
		ETOPSResult er = ETOPSHelper.classify(GeoUtils.greatCircle(jfk, snn, 30));
		assertNotNull(er);
		assertEquals(ETOPS.ETOPS138, er.getResult());
		System.out.println(er.toString());
	}
}