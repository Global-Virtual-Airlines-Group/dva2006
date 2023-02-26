package org.deltava;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.log4j.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.navdata.*;

import junit.framework.TestCase;

public class SimGateMapper extends TestCase {

	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/common";
	private Logger log;
	
	private static final int MAX_DISTANCE = 199;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(SimGateMapper.class);

		// Load JDBC driver
		Class.forName("com.mysql.cj.jdbc.Driver");
		DriverManager.setLoginTimeout(3);
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}
	
	public void testMapGates() throws Exception {
		try (Connection c = DriverManager.getConnection(JDBC_URL, "luke", "14072")) {
			c.setAutoCommit(false);

			// Load airports
			Collection<String> airports = new TreeSet<String>();
			try (PreparedStatement ps = c.prepareStatement("SELECT DISTINCT ICAO FROM GATES")) {
				ps.setFetchSize(500);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						airports.add(rs.getString(1));
				}
			}
			
			/* airports.clear();
			airports.addAll(List.of("KATL", "CYYZ")); */

			// Walk through the airports
			for (String icao : airports) {
				log.info("Loading Gates for " + icao);
				List<LegacyGate> allGates = new ArrayList<LegacyGate>();
				try (PreparedStatement ps = c.prepareStatement("SELECT * FROM GATES WHERE (ICAO=?) ORDER BY SIMVERSION, NAME")) {
					ps.setString(1, icao);
					ps.setFetchSize(200);
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							LegacyGate g = new LegacyGate(rs.getDouble(4), rs.getDouble(5),Simulator.fromVersion(rs.getInt(3), Simulator.UNKNOWN), null);
							g.setCode(icao);
							g.setName(rs.getString(2));
							g.setHeading(rs.getInt(6));
							if (g.getSimulator() != Simulator.UNKNOWN)
								allGates.add(g);
						}
					}
				}

				// Map SoT gates
				Collection<Simulator> sims = allGates.stream().map(LegacyGate::getSimulator).collect(Collectors.toSet());
				Simulator authoritativeSim = sims.contains(Simulator.P3Dv4) ? Simulator.P3Dv4 : Simulator.FS2020;
				List<Gate> gates = allGates.stream().filter(lg -> (lg.getSimulator() == authoritativeSim)).map(lg -> new Gate(lg, 0)).collect(Collectors.toList());
				allGates.removeAll(gates);

				// For each remaining legacy gate, find its closest gate
				if (!gates.isEmpty()) {
					for (Iterator<LegacyGate> i = allGates.iterator(); i.hasNext();) {
						LegacyGate lg = i.next();
						Collections.sort(gates, new GeoGateComparator(lg));
						Gate cg = gates.get(0);
						int dst = lg.distanceFeet(cg);
						if (dst > MAX_DISTANCE) {
							if (lg.getGateType() == GateType.GATE)
								log.warn("No close gate for " + lg.getSimulator().name() + " " + lg.getName() + " at " + icao + " (" + dst + " ft) - " + cg.getName());
							i.remove();
						} else
							lg.setOldName(cg.getName());
					}
					
					// Write authoritative gates
					/* try (PreparedStatement ps = c.prepareStatement("INSERT INTO common.GATES (ICAO, NAME, LATITUDE, LONGITUDE, HDG) VALUES (?, ?, ?, ?, ?) AS NG ON DUPLICATE KEY UPDATE NAME=NG.NAME")) {
						ps.setString(1, icao);
						for (Gate g : gates) {
							ps.setString(2, g.getName());
							ps.setDouble(3, g.getLatitude());
							ps.setDouble(4, g.getLongitude());
							ps.setInt(5, g.getHeading());
							ps.addBatch();
						}
						
						ps.executeBatch();
					} */
					
					// Write the mappings
					try (PreparedStatement ps = c.prepareStatement("INSERT INTO common.GATE_LEGACY (ICAO, NAME, SIMVERSION, NEWNAME) VALUES (?, ?, ?, ?) AS NN ON DUPLICATE KEY UPDATE NEWNAME=NN.NEWNAME")) {
						ps.setString(1, icao);
						for (LegacyGate lg : allGates) {
							ps.setString(2, lg.getName());
							ps.setInt(3, lg.getSimulator().getCode());
							ps.setString(4, lg.getOldName());
							ps.addBatch();
						}
						
						ps.executeBatch();
					}
					
					if (allGates.size() > 0)
						log.info("Wrote " + allGates.size() + " legacy mappings for " + icao);
				}
			}
			
			c.commit();
		}
	}
}