// Copyright 2012, 2015, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.GateComparator;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Airport gate information. 
 * @author Luke
 * @version 8.6
 * @since 5.1
 */

public class GetGates extends DAO {
	
	private static final Cache<CacheableCollection<Gate>> _cache = CacheManager.getCollection(Gate.class, "Gates");
	
	private static final Comparator<Gate> CMP = new GateComparator(GateComparator.USAGE).reversed();

	private static String createKey(ICAOAirport a, Simulator sim) {
		StringBuilder buf = new StringBuilder(a.getICAO());
		buf.append('-').append(sim.name());
		return buf.toString();
	}
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetGates(Connection c) {
		super(c);
	}
	
	/**
	 * Loads specific gates for an ACARS Flight.
	 * @param info the ACARS FlightInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void populate(FlightInfo info) throws DAOException {
		try {
			prepareStatement("SELECT G.*, IFNULL(GROUP_CONCAT(GA.AIRLINE),''), GA.INTL, ND.REGION FROM acars.FLIGHTS F, acars.GATEDATA FG, common.GATES G LEFT JOIN common.GATE_AIRLINES GA ON "
				+ "(G.ICAO=GA.ICAO) AND (G.NAME=GA.NAME) LEFT JOIN common.NAVDATA ND ON (G.ICAO=ND.CODE) AND (ND.ITEMTYPE=?) WHERE (F.ID=?) AND (F.ID=FG.ID) AND (G.SIMVERSION=F.FSVERSION) "
				+ "AND (G.ICAO=FG.ICAO) AND (G.NAME=FG.GATE) GROUP BY G.NAME ORDER BY FG.ISDEPARTURE");
			_ps.setInt(1, Navaid.AIRPORT.ordinal());
			_ps.setInt(2, info.getID());
			for (Gate g : execute()) {
				if (g.getCode().equals(info.getAirportD().getICAO()))
					info.setGateD(g);
				else
					info.setGateA(g);
			}
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
		
		// Check the cache
		String key = createKey(a, sim);
		CacheableCollection<Gate> results = _cache.get(key);
		if (results != null)
			return CollectionUtils.sort(results.clone(), CMP);

		try {
			prepareStatementWithoutLimits("SELECT G.*, IFNULL(GROUP_CONCAT(GA.AIRLINE),''), GA.INTL, ND.REGION, COUNT(GD.ID) AS CNT FROM acars.GATEDATA GD, common.GATES G LEFT JOIN "
				+ "common.GATE_AIRLINES GA ON (G.ICAO=GA.ICAO) AND (G.NAME=GA.NAME) LEFT JOIN common.NAVDATA ND ON (G.ICAO=ND.CODE) AND (ND.ITEMTYPE=?) WHERE (GD.ICAO=?) AND "
				+ "(G.ICAO=GD.ICAO) AND (G.NAME=GD.GATE) AND (G.SIMVERSION=?) GROUP BY G.NAME ORDER BY CNT DESC");
			_ps.setInt(1, Navaid.AIRPORT.ordinal());
			_ps.setString(2, a.getICAO());
			_ps.setInt(3, sim.getCode());
			results = new CacheableSet<Gate>(key);
			results.addAll(execute());
			
			// Add to cache
			_cache.add(results);
			return CollectionUtils.sort(results.clone(), CMP);
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
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT G.*, IFNULL(GROUP_CONCAT(DISTINCT GA.AIRLINE),'') AS AL, IFNULL(GA.INTL,0) AS INTL, ND.REGION, COUNT(GD.ID) AS CNT FROM acars.FLIGHTS F, "
			+ "acars.GATEDATA GD, common.GATES G LEFT JOIN common.GATE_AIRLINES GA ON (G.ICAO=GA.ICAO) AND (G.NAME=GA.NAME) LEFT JOIN common.NAVDATA ND ON (G.ICAO=ND.CODE) AND "
			+ "(ND.ITEMTYPE=?) WHERE (GD.ID=F.ID) AND (GD.ICAO=?) AND (GD.ISDEPARTURE=?) AND (G.ICAO=GD.ICAO) AND (G.NAME=GD.GATE) AND (G.SIMVERSION=?) ");
		if (rp.getAirportD() != null)
			sqlBuf.append("AND (F.AIRPORT_D=?) ");
		if (rp.getAirportA() != null)
			sqlBuf.append("AND (F.AIRPORT_A=?) ");
		sqlBuf.append("GROUP BY G.NAME ORDER BY CNT DESC");
		
		try {
			int pos = 0;
			prepareStatement(sqlBuf.toString());
			_ps.setInt(++pos, Navaid.AIRPORT.ordinal());
			_ps.setString(++pos, (isDeparture ? rp.getAirportD() : rp.getAirportA()).getICAO());
			_ps.setBoolean(++pos, isDeparture);
			_ps.setInt(++pos, sim.getCode());
			if (rp.getAirportD() != null)
				_ps.setString(++pos, rp.getAirportD().getIATA());
			if (rp.getAirportA() != null)
				_ps.setString(++pos, rp.getAirportA().getIATA());
			
			return execute();
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
		Collection<Gate> results = new LinkedHashSet<Gate>();
		for (int x = Math.max(sim.ordinal(), Simulator.FS9.ordinal()); x < Simulator.values().length; x++) {
			Simulator s = Simulator.values()[x];
			results.addAll(getGates(a, s));
		}
		
		return results;
	}

	/*
	 * Helper method to parse Gate result sets.
	 */
	private List<Gate> execute() throws SQLException {
		List<Gate> results = new ArrayList<Gate>();
		try (ResultSet rs = _ps.executeQuery()) {
			boolean hasCounts = (rs.getMetaData().getColumnCount() > 9); int max = 0;
			while (rs.next()) {
				Gate g = new Gate(rs.getDouble(4), rs.getDouble(5));
				g.setCode(rs.getString(1));
				g.setName(rs.getString(2));
				g.setHeading(rs.getInt(6));
				StringUtils.split(rs.getString(7), ",").stream().map(SystemData::getAirline).filter(Objects::nonNull).forEach(g::addAirline);
				g.setIntl(rs.getBoolean(8));
				g.setRegion(rs.getString(9));
				if (hasCounts) {
					int useCount = rs.getInt(10);
					g.setUseCount(useCount);
					max = Math.max(max, useCount);					
					if ((useCount > (max / 8)) || (max < 20))
						results.add(g);
					else
						break;					
				} else
					results.add(g);
			}
		}
		
		_ps.close();
		return results;
	}
}