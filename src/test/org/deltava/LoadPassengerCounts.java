// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import org.deltava.beans.econ.*;
import org.deltava.beans.schedule.Aircraft;
import org.deltava.beans.stats.AirlineTotals;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class LoadPassengerCounts extends TestCase {
	
	private static Logger log;

	private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/dva";

	private Connection _c;
	private EconomyInfo _econ;

	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(LoadPassengerCounts.class);
		
		SystemData.init();
		
		// Load economy data
		_econ = new EconomyInfo(SystemData.getDouble("econ.targetLoad", 0.8d), SystemData.getDouble("econ.targetAmplitude", 0.125));
		_econ.setMinimumLoad(SystemData.getDouble("econ.minimumLoad", 0.25));
		_econ.setStartDate(AirlineTotals.BIRTHDATE.getTime());
		_econ.setHourlyFactor(SystemData.getDouble("econ.hourlyFactor", 0.0));
		_econ.setYearlyCycleLength(SystemData.getInt("econ.yearlyCycleLength", 365));
		_econ.setHourlyCycleLength(SystemData.getInt("econ.hourlyCycleLength", 24));
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetUserData uddao = new GetUserData(_c);
		SystemData.add("apps", uddao.getAirlines(true));
		GetAirport apdao = new GetAirport(_c);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(_c);
		SystemData.add("airlines", aldao.getAll());
	}

	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testSetLoadFactors() throws SQLException, DAOException {
		Map<String, Aircraft> acCache = new HashMap<String, Aircraft>();
		LoadFactor lf = new LoadFactor(_econ);
		
		PreparedStatement ps2 = _c.prepareStatement("UPDATE PIREPS SET PAX=?, LOADFACTOR=? WHERE (ID=?)");
		
		int totalFlights = 0;
		GetAircraft acdao = new GetAircraft(_c);
		PreparedStatement ps = _c.prepareStatement("SELECT ID, IFNULL(SUBMITTED, DATE), EQTYPE FROM PIREPS WHERE (LOADFACTOR=0) AND (STATUS<>0)");
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			totalFlights++;
			String eqType = rs.getString(3);
			Aircraft a = acCache.get(eqType);
			if (a == null) {
				a = acdao.get(eqType);
				if (a == null) {
					log.warn("Unknown aircraft type - " + eqType);
					continue;
				}
				
				acCache.put(eqType, a);
			}

			// Calculate passengers/load factor
			double loadFactor = lf.generate(rs.getTimestamp(2));
			int pax = (int) Math.round(a.getSeats() * loadFactor);
			
			// Update
			ps2.setInt(1, pax);
			ps2.setDouble(2, loadFactor);
			ps2.setInt(3, rs.getInt(1));
			ps2.executeUpdate();
			
			if ((totalFlights % 250) == 0) {
				log.info(totalFlights + " flights updated");
				_c.commit();
			}
		}
		
		// Commit
		ps.close();
		ps2.close();
		_c.commit();
		log.info(totalFlights + " flights updated");
	}
}