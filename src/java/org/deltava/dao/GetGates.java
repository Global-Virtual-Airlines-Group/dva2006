// Copyright 2012, 2015, 2018, 2019, 2020, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.Simulator;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.GateUsage;

import org.deltava.comparators.GateComparator;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Airport gate information. 
 * @author Luke
 * @version 10.3
 * @since 5.1
 */

public class GetGates extends DAO {
	
	private static final Cache<CacheableCollection<Gate>> _cache = CacheManager.getCollection(Gate.class, "Gates");
	private static final Cache<GateUsage> _useCache = CacheManager.get(GateUsage.class, "GateUsage");
	
	private static final Comparator<Gate> CMP = new GateComparator(GateComparator.USAGE).reversed();
	private static final int DAY_RANGE = 365;

	private static void populateFlight(FlightInfo fi, Gate g) {
		if (g.getCode().equals(fi.getAirportD().getICAO()))
			fi.setGateD(g);
		else
			fi.setGateA(g);
	}
	
	private static void checkSimulator(Simulator sim) {
		if ((sim == null) || (sim == Simulator.UNKNOWN))
			throw new IllegalArgumentException(String.format("Invalid simulator - %s", sim));
	}
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetGates(Connection c) {
		super(c);
	}
	
	/**
	 * Returns all Airport gates.
	 * @return a Collection of Gates
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Gate> getAll() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT G.*, IFNULL(GROUP_CONCAT(DISTINCT GA.AIRLINE),'') AS AL, IFNULL(GA.ZONE,0) AS ZONE, ND.REGION FROM common.GATES G LEFT JOIN "
			+ "common.GATE_AIRLINES GA ON ((G.ICAO=GA.ICAO) AND (G.NAME=GA.NAME)) LEFT JOIN common.NAVDATA ND ON ((G.ICAO=ND.CODE) AND (ND.ITEMTYPE=?)) GROUP BY G.ICAO, G.NAME, G.SIMVERSION")) {
			ps.setInt(1, Navaid.AIRPORT.ordinal());
			ps.setFetchSize(2500);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads specific gates for an ACARS Flight.
	 * @param info the ACARS FlightInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void populate(FlightInfo info) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT G.*, IFNULL(GROUP_CONCAT(DISTINCT GA.AIRLINE),''), GA.ZONE, ND.REGION FROM acars.FLIGHTS F, acars.GATEDATA FG, common.GATES G "
			+ "LEFT JOIN common.GATE_AIRLINES GA ON ((G.ICAO=GA.ICAO) AND (G.NAME=GA.NAME)) LEFT JOIN common.NAVDATA ND ON ((G.ICAO=ND.CODE) AND (ND.ITEMTYPE=?)) WHERE (F.ID=?) AND "
			+ "(F.ID=FG.ID) AND (G.SIMVERSION=F.FSVERSION) AND (G.ICAO=FG.ICAO) AND (G.NAME=FG.GATE) GROUP BY G.NAME LIMIT 2")) {
			ps.setInt(1, Navaid.AIRPORT.ordinal());
			ps.setInt(2, info.getID());
			execute(ps).forEach(g -> populateFlight(info, g));
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns popular Gates for a particular Airport.
	 * @param a the Airport
	 * @param sim the Simulator
	 * @return a List of Gates, ordered by popularity
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Gate> getGates(ICAOAirport a, Simulator sim) throws DAOException {
		checkSimulator(sim);
		
		// Check the cache
		String key = String.format("AP-%s-%s", a.getICAO(), sim.name());
		CacheableCollection<Gate> results = _cache.get(key);
		if (results != null)
			return CollectionUtils.sort(results, CMP); // this already clones the src

		try (PreparedStatement ps = prepareWithoutLimits("SELECT G.*, IFNULL(GROUP_CONCAT(DISTINCT GA.AIRLINE),''), IFNULL(GA.ZONE,0), ND.REGION, COUNT(GD.ID) AS CNT FROM common.GATES G LEFT JOIN "
			+ "common.GATE_AIRLINES GA ON (G.ICAO=GA.ICAO) AND (G.NAME=GA.NAME) LEFT JOIN common.NAVDATA ND ON (G.ICAO=ND.CODE) AND (ND.ITEMTYPE=?) LEFT JOIN acars.GATEDATA GD ON "
			+ "(G.ICAO=GD.ICAO) AND (G.NAME=GD.GATE) WHERE (G.ICAO=?) AND (G.SIMVERSION=?) GROUP BY G.NAME")) {
			ps.setInt(1, Navaid.AIRPORT.ordinal());
			ps.setString(2, a.getICAO());
			ps.setInt(3, sim.getCode());
			results = new CacheableSet<Gate>(key);
			results.addAll(execute(ps));
			
			// Add to cache
			_cache.add(results);
			return CollectionUtils.sort(results, CMP);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns popular Gates for a particular Route.
	 * @param rp the RoutePair
	 * @param sim the Simulator
	 * @param isDeparture TRUE if returning preferred departure Gate, otherwise FALSE
	 * @return a List of Gates, ordered by popularity
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Gate> getPopularGates(RoutePair rp, Simulator sim, boolean isDeparture) throws DAOException {
		checkSimulator(sim);
		
		// Get the Gates
		Airport a = isDeparture ? rp.getAirportD() : rp.getAirportA();
		List<Gate> gates = getGates(a, sim);
		
		// Check the cache
		GateUsage gu = new GateUsage(rp, isDeparture, DAY_RANGE);
		GateUsage gateUse = _useCache.get(gu.cacheKey());
		if (gateUse != null) {
			boolean useRecent = (gateUse.getRecentSize() > 3);
			List<Gate> results = gates.stream().map(g -> new Gate(g, useRecent ? gateUse.getRecentUsage(g.getName()) : gateUse.getTotalUsage(g.getName()))).collect(Collectors.toList()); // clone the gate
			return CollectionUtils.sort(results, CMP);
		}
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT G.NAME, COUNT(GD.ID) AS TCNT, SUM(IF(F.CREATED>DATE_SUB(NOW(), INTERVAL ? DAY),1,0)) AS CNT FROM acars.FLIGHTS F, acars.GATEDATA GD, common.GATES G WHERE (GD.ID=F.ID) "
			+ "AND (GD.ICAO=?) AND (GD.ISDEPARTURE=?) AND (G.ICAO=GD.ICAO) AND (G.NAME=GD.GATE) ");
		if (rp.getAirportD() != null)
			sqlBuf.append("AND (F.AIRPORT_D=?) ");
		if (rp.getAirportA() != null)
			sqlBuf.append("AND (F.AIRPORT_A=?) ");
		sqlBuf.append("GROUP BY G.NAME");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) { 
			int pos = 0;
			ps.setInt(++pos, DAY_RANGE);
			ps.setString(++pos, a.getICAO());
			ps.setBoolean(++pos, isDeparture);
			if (rp.getAirportD() != null)
				ps.setString(++pos, rp.getAirportD().getIATA());
			if (rp.getAirportA() != null)
				ps.setString(++pos, rp.getAirportA().getIATA());
			
			// Load gate usage
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String g = rs.getString(1);
					gu.addGate(g, rs.getInt(2), rs.getInt(3));
				}
			}
			
			// Determine whether to use recent, or total
			_useCache.add(gu);
			boolean useRecent = (gu.getRecentSize() > 3);
			List<Gate> results = gates.stream().map(g -> new Gate(g, useRecent ? gu.getRecentUsage(g.getName()) : gu.getTotalUsage(g.getName()))).collect(Collectors.toList()); // clone the gate
			return CollectionUtils.sort(results, CMP);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads a specific gate.
	 * @param a the ICAOAirport
	 * @param sim the Simulator
	 * @param code the Gate name
	 * @return a Collection of Gate beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Gate getGate(ICAOAirport a, Simulator sim, String code) throws DAOException {
		checkSimulator(sim);
		Collection<Gate> gates = getGates(a, sim);
		return gates.stream().filter(g -> (g.getName().equals(code))).findAny().orElse(null);
	}
	
	/**
	 * Loads all gates for a particular Airport in several simulators.
	 * @param a the ICAOAirport
	 * @param sim the earliest Simulator to add
	 * @return a Collection of Gate beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Gate> getAllGates(ICAOAirport a, Simulator sim) throws DAOException {
		Simulator maxSim = (sim.ordinal() > Simulator.P3Dv4.ordinal()) ? Simulator.XP11 : Simulator.P3Dv4;
		Collection<Gate> results = new LinkedHashSet<Gate>();  
		for (int x = Math.max(sim.ordinal(), Simulator.FS9.ordinal()); x <= maxSim.ordinal(); x++) {
			Simulator s = Simulator.values()[x];
			results.addAll(getGates(a, s));
		}
		
		return results;
	}

	/*
	 * Helper method to parse Gate result sets.
	 */
	private static List<Gate> execute(PreparedStatement ps) throws SQLException {
		List<Gate> results = new ArrayList<Gate>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean hasUseCount = (rs.getMetaData().getColumnCount() > 9);
			while (rs.next()) {
				Gate g = new Gate(rs.getDouble(4), rs.getDouble(5));
				g.setCode(rs.getString(1));
				g.setName(rs.getString(2));
				g.setSimulator(Simulator.fromVersion(rs.getInt(3), Simulator.UNKNOWN));
				g.setHeading(rs.getInt(6));
				StringUtils.split(rs.getString(7), ",").stream().map(SystemData::getAirline).filter(Objects::nonNull).forEach(g::addAirline);
				g.setZone(GateZone.values()[rs.getInt(8)]);
				g.setRegion(rs.getString(9));
				if (hasUseCount) g.setUseCount(rs.getInt(10));
				results.add(g);
			}
		}
		
		return results;
	}
}