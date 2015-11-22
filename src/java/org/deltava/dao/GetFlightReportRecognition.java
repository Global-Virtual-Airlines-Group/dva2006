// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.flight.FlightReport;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to get Flight Report IDs for Pilot recognition.
 * @author Luke
 * @version 6.3
 * @since 1.0
 */

public class GetFlightReportRecognition extends DAO {
	
	private static final Cache<CacheableList<Integer>> _cache = CacheManager.getCollection(Integer.class, "GreasedLandings"); 
	
	private static final int OPT_VSPEED = -250;
	private static final int OPT_DISTANCE = 1250;
	
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
	 * @return a List of ACARSFlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Integer> getGreasedLandings() throws DAOException {
		
		// Check the cache
		CacheableList<Integer> results = _cache.get("ALL" + _dayFilter);
		if (results != null)
			return results;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, (((ABS(?-VSPEED) * 3) + (ABS(?-RWYDISTANCE) * 2)) / 5) AS FACT "
			+ "FROM FLIGHTSTATS_LANDING ");
		if (_dayFilter > 0)
			sqlBuf.append(" WHERE (DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY FACT, DATE DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, OPT_VSPEED);
			_ps.setInt(2, OPT_DISTANCE);
			if (_dayFilter > 0)
				_ps.setInt(3, _dayFilter);

			// Add to the cache
			results = new CacheableList<Integer>("ALL" + _dayFilter);
			results.addAll(execute());
			_cache.add(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves ACARS Flight Reports logged by staff members.
	 * @return a List of ACARSFlightReport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Integer> getStaffReports() throws DAOException {
		
		// Check the cache
		CacheableList<Integer> results = _cache.get("STAFF" + _dayFilter);
		if (results != null)
			return results;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT L.ID, (((ABS(?-L.VSPEED) * 3) + (ABS(?-L.RWYDISTANCE) * 2)) / 5) AS FACT "
			+ "FROM STAFF S LEFT JOIN FLIGHTSTATS_LANDING L ON (L.PILOT_ID=S.ID)"); 
		if (_dayFilter > 0)
			sqlBuf.append(" WHERE (L.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY FACT, L.DATE DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, OPT_VSPEED);
			_ps.setInt(2, OPT_DISTANCE);
			if (_dayFilter > 0)
				_ps.setInt(3, _dayFilter);

			// Add to the cache
			results = new CacheableList<Integer>("STAFF" + _dayFilter);
			results.addAll(execute());
			_cache.add(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves ACARS Flight Reports for a particular equipment type.
	 * @param eqType the equipment type
	 * @return a List of ACARSFlightReport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Integer> getGreasedLandings(String eqType) throws DAOException {
		
		// Check the cache
		CacheableList<Integer> results = _cache.get("EQ" + eqType + "$" + _dayFilter);
		if (results != null)
			return results;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, (((ABS(?-VSPEED) * 3) + (ABS(?-RWYDISTANCE) * 2)) / 5) AS FACT "
			+ "FROM FLIGHTSTATS_LANDING WHERE (EQTYPE=?)");
		if (_dayFilter > 0)
			sqlBuf.append("AND (DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY FACT, DATE DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, OPT_VSPEED);
			_ps.setInt(2, OPT_DISTANCE);
			_ps.setString(3, eqType);
			if (_dayFilter > 0)
				_ps.setInt(4, _dayFilter);
			
			// Add to the cache
			results = new CacheableList<Integer>("EQ" + eqType + "$" + _dayFilter);
			results.addAll(execute());
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
		buf.append(" GROUP BY EQTYPE HAVING (CNT >= ?) ORDER BY CNT DESC");
		
		try {
			int pos = 0;
			prepareStatement(buf.toString());
			if (_dayFilter > 0)
				_ps.setInt(++pos, _dayFilter);
			_ps.setInt(++pos, minLegs);

			// Execute the query
			List<String> results = new ArrayList<String>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}

			_ps.close();
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
		try {
			prepareStatementWithoutLimits("SELECT COUNT(PR.ID) FROM PIREPS PR, PROMO_EQ PE WHERE (PR.ID=PE.ID) "
					+ "AND (PR.PILOT_ID=?) AND (PE.EQTYPE=?) AND (PR.STATUS=?) LIMIT 1");
			_ps.setInt(1, pilotID);
			_ps.setString(2, eqType);
			_ps.setInt(3, FlightReport.OK);

			// Execute the query
			int results = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					results = rs.getInt(1);
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to load Flight Report IDs.
	 */
	private List<Integer> execute() throws SQLException {
		List<Integer> results = new ArrayList<Integer>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next())
				results.add(Integer.valueOf(rs.getInt(1)));
		}
		
		return results;
	}
}