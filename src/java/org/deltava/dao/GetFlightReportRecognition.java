// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2018, 2019, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.flight.*;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to get Flight Report IDs for Pilot recognition.
 * @author Luke
 * @version 10.4
 * @since 1.0
 */

public class GetFlightReportRecognition extends DAO {
	
	private static final Cache<CacheableList<Integer>> _cache = CacheManager.getCollection(Integer.class, "GreasedLandings"); 
	
	private int _dayFilter;

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetFlightReportRecognition(Connection c) {
		super(c);
	}
	
	/**
	 * Sets the maximum number of days in the past to include.
	 * @param days the number of days
	 * @since 2.1
	 */
	public void setDayFilter(int days) {
		_dayFilter = Math.max(0, days);
	}

	/**
	 * Returns Flight Reports with the smoothest touchdown speed.
	 * @return a List of ACARSFlightReport database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Integer> getGreasedLandings() throws DAOException {
		
		// Check the cache
		String cacheKey = "ALL!" + _dayFilter;
		CacheableList<Integer> results = _cache.get(cacheKey);
		if ((results != null) && (results.size() >= _queryMax))
			return results.clone().subList(0, _queryMax);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, SCORE FROM FLIGHTSTATS_LANDING ");
		if (_dayFilter > 0)
			sqlBuf.append(" WHERE (DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY SCORE DESC, DATE DESC");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			if (_dayFilter > 0)
				ps.setInt(1, _dayFilter);

			// Add to the cache
			results = new CacheableList<Integer>(cacheKey);
			results.addAll(execute(ps));
			_cache.add(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves ACARS Flight Reports logged by staff members.
	 * @return a List of ACARSFlightReport database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Integer> getStaffReports() throws DAOException {
		
		// Check the cache
		String cacheKey = "STAFF!" + _dayFilter;
		CacheableList<Integer> results = _cache.get(cacheKey);
		if ((results != null) && (results.size() >= _queryMax))
			return results.clone().subList(0, _queryMax);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT L.ID, L.SCORE FROM STAFF S LEFT JOIN FLIGHTSTATS_LANDING L ON (L.PILOT_ID=S.ID)"); 
		if (_dayFilter > 0)
			sqlBuf.append(" WHERE (L.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY SCORE DESC, L.DATE DESC");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			if (_dayFilter > 0)
				ps.setInt(1, _dayFilter);

			// Add to the cache
			results = new CacheableList<Integer>(cacheKey);
			results.addAll(execute(ps));
			_cache.add(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves ACARS Flight Reports for a particular equipment type.
	 * @param eqType the equipment type
	 * @return a List of ACARSFlightReport database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Integer> getGreasedLandings(String eqType) throws DAOException {
		
		// Check the cache
		String cacheKey = "EQ!" + eqType + "$" + _dayFilter;
		CacheableList<Integer> results = _cache.get(cacheKey);
		if ((results != null) && (results.size() >= _queryMax))
			return results.clone().subList(0, _queryMax);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, SCORE FROM FLIGHTSTATS_LANDING WHERE (EQTYPE=?)");
		if (_dayFilter > 0)
			sqlBuf.append("AND (DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY SCORE DESC, DATE DESC");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, eqType);
			if (_dayFilter > 0)
				ps.setInt(2, _dayFilter);
			
			// Add to the cache
			results = new CacheableList<Integer>(cacheKey);
			results.addAll(execute(ps));
			_cache.add(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all equipment types with ACARS Flight Reports.
	 * @param minLegs the minimum number of Flight Reports required for inclusion
	 * @return a List of equipment types
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<String> getACARSEquipmentTypes(int minLegs) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT EQTYPE, COUNT(ID) AS CNT FROM FLIGHTSTATS_LANDING");
		if (_dayFilter > 0)
			buf.append(" WHERE (DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		buf.append(" GROUP BY EQTYPE HAVING (CNT >= ?)");
		
		try (PreparedStatement ps = prepare(buf.toString())) {
			int pos = 0;
			if (_dayFilter > 0)
				ps.setInt(++pos, _dayFilter);
			ps.setInt(++pos, minLegs);

			// Execute the query
			List<String> results = new ArrayList<String>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns top Landings for a particular Pilot.
	 * @param pilotID the Pilots' database ID
	 * @return a List of LandingInfo beans, sorted by descending score
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Integer> getGreasedLandings(int pilotID) throws DAOException {
		
		// Check the cache
		String cacheKey = "USR!" + pilotID + "$" + _dayFilter;
		CacheableList<Integer> results = _cache.get(cacheKey);
		if ((results != null) && (results.size() >= _queryMax))
			return results.clone().subList(0, _queryMax);
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, SCORE FROM FLIGHTSTATS_LANDING WHERE (PILOT_ID=?)");
		if (_dayFilter > 0)
			sqlBuf.append("AND (DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY SCORE DESC, DATE DESC");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, pilotID);
			if (_dayFilter > 0) 
				ps.setInt(2, _dayFilter);
			
			// Add to the cache
			results = new CacheableList<Integer>(cacheKey);
			results.addAll(execute(ps));
			_cache.add(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves the number of legs a Pilot has completed that count towards promotion to Captain.
	 * @param pilotID the Pilot's database ID
	 * @param eqType the equipment program name
	 * @return the number of completed legs
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getPromotionCount(int pilotID, String eqType) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT COUNT(PR.ID) FROM PIREPS PR, PROMO_EQ PE WHERE (PR.ID=PE.ID) AND (PR.PILOT_ID=?) AND (PE.EQTYPE=?) AND (PR.STATUS=?) LIMIT 1")) {
			ps.setInt(1, pilotID);
			ps.setString(2, eqType);
			ps.setInt(3, FlightStatus.OK.ordinal());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to load Flight Report IDs.
	 */
	private static List<Integer> execute(PreparedStatement ps) throws SQLException {
		List<Integer> results = new ArrayList<Integer>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next())
				results.add(Integer.valueOf(rs.getInt(1)));
		}
		
		return results;
	}
}