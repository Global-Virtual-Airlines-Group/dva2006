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
	
	private static final String JDBC_URL = "jdbc:mysql://pollux.gvagroup.org/dva";

	private Connection _c;
	private EconomyInfo _econ;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		SystemData.init();
		
		// Load economy data
		_econ = new EconomyInfo(SystemData.getDouble("econ.targetLoad", 0.8d), SystemData.getDouble("econ.targetAmplitude", 0.125));
		_econ.setMinimumLoad(SystemData.getDouble("econ.minimumLoad", 0.25));
		_econ.setStartDate(AirlineTotals.BIRTHDATE);
		_econ.setHourlyFactor(SystemData.getDouble("econ.hourlyFactor", 0.0));
		_econ.setYearlyCycleLength(SystemData.getInt("econ.yearlyCycleLength", 365));
		_econ.setHourlyCycleLength(SystemData.getInt("econ.hourlyCycleLength", 24));
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "test", "test");
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

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testSetLoadFactors() throws SQLException, DAOException {
		Map<String, Aircraft> acCache = new HashMap<String, Aircraft>();
		LoadFactor lf = new LoadFactor(_econ);
		
		int totalFlights = 0;
		try (PreparedStatement ps2 = _c.prepareStatement("UPDATE PIREPS SET PAX=?, LOADFACTOR=? WHERE (ID=?)")) {
			GetAircraft acdao = new GetAircraft(_c);
			try (PreparedStatement ps = _c.prepareStatement("SELECT ID, IFNULL(SUBMITTED, DATE), EQTYPE FROM PIREPS WHERE (LOADFACTOR=0) AND (STATUS<>0)")) {
				ps.setFetchSize(100);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						totalFlights++;
						String eqType = rs.getString(3);
						Aircraft a = acCache.get(eqType);
						if (a == null) {
							a = acdao.get(eqType);
							if (a == null) {
								System.out.println("Unknown aircraft type - " + eqType);
								continue;
							}
							
							acCache.put(eqType, a);
						}

						// Calculate passengers/load factor
						double loadFactor = lf.generate(rs.getTimestamp(2).toInstant());
						int pax = (int) Math.round(a.getOptions(SystemData.get("airline.code")).getSeats() * loadFactor);
			
						// Update
						ps2.setInt(1, pax);
						ps2.setDouble(2, loadFactor);
						ps2.setInt(3, rs.getInt(1));
						ps2.addBatch();
						
						if ((totalFlights % 50) == 0) {
							System.out.println(totalFlights + " flights updated");
							ps2.executeBatch();
							_c.commit();
						}
					}
				}
			}
		}
		
		_c.commit();
		System.out.println(totalFlights + " flights updated");
	}
}