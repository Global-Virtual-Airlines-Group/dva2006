// Copyright 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

public class RunwayPIREPLoader extends TestCase {
	
	private static Logger log;

	private static final String db = "afv";
	private static final String JDBC_URL = "jdbc:mysql://pollux.gvagroup.org/" + db;
	//private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/" + db;
	
	private Connection _c;

	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(RunwayPIREPLoader.class);
		
		SystemData.init();
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetAirport apdao = new GetAirport(_c);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(_c);
		SystemData.add("airlines", aldao.getAll());
		
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testSetRunways() throws Exception {
		
		// Load the PIREPs
		Statement s = _c.createStatement();
		s.setFetchSize(2000);
		ResultSet rs = s.executeQuery("SELECT ACARS_ID FROM ACARS_PIREPS WHERE (TAKEOFF_HDG=-1)");
		
		Collection<Integer> IDs = new LinkedHashSet<Integer>();
		while (rs.next())
			IDs.add(new Integer(rs.getInt(1)));
		
		rs.close();
		s.close();
		
		// Build the update statements
		PreparedStatement tps = _c.prepareStatement("UPDATE ACARS_PIREPS SET TAKEOFF_LAT=?, TAKEOFF_LNG=?,"
			+ "TAKEOFF_ALT=? WHERE (ACARS_ID=?)");
		PreparedStatement lps = _c.prepareStatement("UPDATE ACARS_PIREPS SET LANDING_LAT=?, LANDING_LNG=?, "
			+ "LANDING_ALT=? WHERE (ACARS_ID=?)");

		PreparedStatement rps = _c.prepareStatement("SELECT R.ID, C.PILOT_ID, UD.AIRLINE, R.ISTAKEOFF, R.LATITUDE, R.LONGITUDE, "
				+ "IFNULL(ND.ALTITUDE, 0) FROM common.USERDATA UD, acars.FLIGHTS F, acars.CONS C, acars.RWYDATA R "
				+ "LEFT JOIN common.NAVDATA ND ON (R.ICAO=ND.CODE) AND (ND.ITEMTYPE=0) WHERE (R.ID=F.ID) AND "
				+ "(F.CON_ID=C.ID) AND (UD.ID=C.PILOT_ID) AND (UD.AIRLINE=?) AND (F.ID=?)");
		rps.setString(1, db);
		
		int flightsDone = 0;
		for (Integer id : IDs) {
			rps.setInt(2, id.intValue());
			rs = rps.executeQuery();
			while (rs.next()) {
				boolean isTakeoff = rs.getBoolean(4);
				PreparedStatement ps = isTakeoff ? tps : lps;
				ps.setDouble(1, rs.getDouble(5));
				ps.setDouble(2, rs.getDouble(6));
				ps.setInt(3, rs.getInt(7));
				ps.setInt(4, rs.getInt(1));
				ps.executeUpdate();
				flightsDone++;
				if ((flightsDone % 100) == 0)
					log.info(flightsDone + " flights updated");
			}
			
			rs.close();
		}
		
		rs.close();
		tps.close();
		lps.close();
		_c.commit();
	}
}