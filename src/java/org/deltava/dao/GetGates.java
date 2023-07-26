// Copyright 2012, 2015, 2018, 2019, 2020, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.GateUsage;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Airport gate information. 
 * @author Luke
 * @version 11.1
 * @since 5.1
 */

public class GetGates extends DAO {
	
	private static final Cache<CacheableList<Gate>> _cache = CacheManager.getCollection(Gate.class, "Gates");
	private static final Cache<GateUsage> _useCache = CacheManager.get(GateUsage.class, "GateUsage");
	private static final Cache<CacheableList<Airport>> _pairCache = CacheManager.getCollection(Airport.class, "GatePairs");
	
	private static void populateFlight(FlightInfo fi, Gate g) {
		if (g.getCode().equals(fi.getAirportD().getICAO()))
			fi.setGateD(g);
		else
			fi.setGateA(g);
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
			+ "common.GATE_AIRLINES GA ON ((G.ICAO=GA.ICAO) AND (G.NAME=GA.NAME)) LEFT JOIN common.NAVDATA ND ON ((G.ICAO=ND.CODE) AND (ND.ITEMTYPE=?)) GROUP BY G.ICAO, G.NAME")) {
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT G.*, IFNULL(GROUP_CONCAT(DISTINCT GA.AIRLINE),'') AS AL, IFNULL(GA.ZONE,0) AS ZONE, ND.REGION FROM acars.GATEDATA FG, common.GATES G "
			+ "LEFT JOIN common.GATE_AIRLINES GA ON ((G.ICAO=GA.ICAO) AND (G.NAME=GA.NAME)) LEFT JOIN common.NAVDATA ND ON ((G.ICAO=ND.CODE) AND (ND.ITEMTYPE=?)) WHERE (FG.ID=?) AND "
			+ "(G.ICAO=FG.ICAO) AND (G.ICAO=FG.ICAO) AND (G.NAME=FG.GATE) GROUP BY G.NAME LIMIT 2")) {
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
	 * @return a List of Gates, ordered by name
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Gate> getGates(ICAOAirport a) throws DAOException {
		
		// Check the cache
		String key = String.format("AP-%s", a.getICAO());
		CacheableList<Gate> results = _cache.get(key);
		if (results != null)
			return results.clone();
		
		try (PreparedStatement ps = prepareWithoutLimits("SELECT G.*, IFNULL(GROUP_CONCAT(DISTINCT GA.AIRLINE),''), IFNULL(GA.ZONE,0), ND.REGION FROM common.GATES G LEFT JOIN "
			+ "common.GATE_AIRLINES GA ON (G.ICAO=GA.ICAO) AND (G.NAME=GA.NAME) LEFT JOIN common.NAVDATA ND ON (G.ICAO=ND.CODE) AND (ND.ITEMTYPE=?) LEFT JOIN acars.GATEDATA GD ON "
			+ "(G.ICAO=GD.ICAO) AND (G.NAME=GD.GATE) WHERE (G.ICAO=?) GROUP BY G.NAME ORDER BY G.NAME")) {
			ps.setInt(1, Navaid.AIRPORT.ordinal());
			ps.setString(2, a.getICAO());
			results = new CacheableList<Gate>(key);
			results.addAll(execute(ps));
			
			// Add to cache
			_cache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns popular Gates for a particular Route.
	 * @param rp the RoutePair
	 * @param isDeparture TRUE if returning preferred departure Gate, otherwise FALSE
	 * @param dbName the database name
	 * @return a List of Gates, ordered by popularity
	 * @throws DAOException if a JDBC error occurs
	 */
	public GateUsage getUsage(RoutePair rp, boolean isDeparture, String dbName) throws DAOException {
		
		// Check the cache
		GateUsage gu = new GateUsage(rp, isDeparture);
		GateUsage gateUse = _useCache.get(gu.cacheKey());
		if (gateUse != null)
			return gateUse.clone();
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT GATE, AIRLINE, SUM(USECOUNT) AS CNT FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".FLIGHTSTATS_GATE WHERE (ISDEPARTURE=?) ");
		if (rp.getAirportD() != null)
			sqlBuf.append("AND (AIRPORT_D=?) ");
		if (rp.getAirportA() != null)
			sqlBuf.append("AND (AIRPORT_A=?) ");
		sqlBuf.append("GROUP BY GATE, AIRLINE ORDER BY CNT DESC");

		gu = new GateUsage(rp, isDeparture);
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) { 
			int pos = 0;
			ps.setBoolean(++pos, isDeparture);
			if (rp.getAirportD() != null)
				ps.setString(++pos, rp.getAirportD().getIATA());
			if (rp.getAirportA() != null)
				ps.setString(++pos, rp.getAirportA().getIATA());
			
			// Load gate usage
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					gu.add(rs.getString(1), SystemData.getAirline(rs.getString(2)), rs.getInt(3));
			}
			
			_useCache.add(gu);
			return gu.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns available Airports for Gate usage statistics for a given Airport.
	 * @param a the Airport
	 * @param isDeparture TRUE for departure Gate statistics, otherwise FALSE
	 * @return a List of Airports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Airport> getUsagePairs(Airport a, boolean isDeparture) throws DAOException {
		
		// Check the cache
		String cacheKey = String.format("%s-%s", a.getICAO(), isDeparture ? "D" : "A");
		CacheableList<Airport> results = _pairCache.get(cacheKey);
		if (results != null)
			return results.clone();
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT GD.ICAO FROM acars.FLIGHTS F, acars.GATEDATA GD WHERE (F.ID=GD.ID) AND (GD.ISDEPARTURE=?) AND ");
		sqlBuf.append(isDeparture ? "(F.AIRPORT_D=?)" : "(F.AIRPORT_A=?)");
		sqlBuf.append(" AND (F.CREATED>DATE_SUB(NOW(), INTERVAL ? YEAR))");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setBoolean(1, !isDeparture);
			ps.setString(2, a.getIATA());
			ps.setInt(3, GateUsage.GATE_USAGE_YEARS);
			
			results = new CacheableList<Airport>(cacheKey);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(SystemData.getAirport(rs.getString(1)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads a specific gate.
	 * @param a the ICAOAirport
	 * @param code the Gate name
	 * @return a Collection of Gate beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Gate getGate(ICAOAirport a, String code) throws DAOException {
		Collection<Gate> gates = getGates(a);
		return gates.stream().filter(g -> g.getName().equals(code)).findAny().orElse(null);
	}
	
	/*
	 * Helper method to parse Gate result sets.
	 */
	private static List<Gate> execute(PreparedStatement ps) throws SQLException {
		List<Gate> results = new ArrayList<Gate>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Gate g = new Gate(rs.getDouble(3), rs.getDouble(4));
				g.setCode(rs.getString(1));
				g.setName(rs.getString(2));
				g.setHeading(rs.getInt(5));
				// skip LL
				StringUtils.split(rs.getString(7), ",").stream().map(SystemData::getAirline).filter(Objects::nonNull).forEach(g::addAirline);
				g.setZone(GateZone.values()[rs.getInt(8)]);
				g.setRegion(rs.getString(9));
				results.add(g);
			}
		}
		
		return results;
	}
}