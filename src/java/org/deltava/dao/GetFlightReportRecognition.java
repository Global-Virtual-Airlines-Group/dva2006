// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.FlightReport;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to get Flight Reports for Pilot recognition.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class GetFlightReportRecognition extends GetFlightReports implements CachingDAO {
	
	private static final int MIN_ACARS_CLIENT = 61;
	
	private static final Cache<CacheableList<FlightReport>> _cache = 
		new ExpiringCache<CacheableList<FlightReport>>(16, 600);
	
	private int _dayFilter;

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetFlightReportRecognition(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the number of cache hits.
	 * @return the number of hits
	 */
	public int getHits() {
		return _cache.getHits();
	}

	/**
	 * Returns the number of cache requests.
	 * @return the number of requests
	 */
	public int getRequests() {
		return _cache.getRequests();
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
	public List<FlightReport> getGreasedLandings() throws DAOException {
		
		// Check the cache
		CacheableList<FlightReport> results = _cache.get("ALL" + _dayFilter);
		if (results != null)
			return results;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, APR.* FROM PIREPS PR LEFT JOIN "
				+ "PIREP_COMMENT PC ON (PC.ID=PR.ID) LEFT JOIN ACARS_PIREPS APR ON (PR.ID=APR.ID) LEFT JOIN "
				+ "acars.FLIGHTS F ON (F.ID=APR.ACARS_ID) LEFT JOIN acars.CONS C ON (C.ID=F.CON_ID) WHERE "
				+ "(C.CLIENT_BUILD >= ?) AND (PR.STATUS=?) AND (APR.LANDING_VSPEED < 0)");
		if (_dayFilter > 0)
			sqlBuf.append(" AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY APR.LANDING_VSPEED DESC, PR.DATE DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, MIN_ACARS_CLIENT);
			_ps.setInt(2, FlightReport.OK);
			if (_dayFilter > 0)
				_ps.setInt(3, _dayFilter);

			// Add to the cache
			results = new CacheableList<FlightReport>("ALL" + _dayFilter);
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
	public List<FlightReport> getStaffReports() throws DAOException {
		
		// Check the cache
		CacheableList<FlightReport> results = _cache.get("STAFF" + _dayFilter);
		if (results != null)
			return results;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, APR.* FROM STAFF S LEFT JOIN PIREPS PR "
				+ "ON (PR.PILOT_ID=S.ID) LEFT JOIN PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ACARS_PIREPS APR "
				+ "ON (PR.ID=APR.ID) LEFT JOIN acars.FLIGHTS F ON (F.ID=APR.ACARS_ID) LEFT JOIN acars.CONS C ON "
				+ "(C.ID=F.CON_ID) WHERE (C.CLIENT_BUILD >= ?) AND (PR.STATUS=?) AND (APR.LANDING_VSPEED < 0)");
		if (_dayFilter > 0)
			sqlBuf.append(" AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY APR.LANDING_VSPEED DESC, PR.DATE DESC");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, MIN_ACARS_CLIENT);
			_ps.setInt(2, FlightReport.OK);
			if (_dayFilter > 0)
				_ps.setInt(3, _dayFilter);

			// Add to the cache
			results = new CacheableList<FlightReport>("STAFF" + _dayFilter);
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
	public List<FlightReport> getGreasedLandings(String eqType) throws DAOException {
		
		// Check the cache
		CacheableList<FlightReport> results = _cache.get("EQ" + eqType + "$" + _dayFilter);
		if (results != null)
			return results;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, APR.* FROM PIREPS PR LEFT JOIN "
				+ "PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ACARS_PIREPS APR ON (PR.ID=APR.ID) "
				+ "LEFT JOIN acars.FLIGHTS F ON (F.ID=APR.ACARS_ID) LEFT JOIN acars.CONS C ON (C.ID=F.CON_ID) "
				+ "WHERE (PR.EQTYPE=?) AND (C.CLIENT_BUILD >= ?) AND (PR.STATUS=?) AND (APR.LANDING_VSPEED < 0)");
		if (_dayFilter > 0)
			sqlBuf.append(" AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY APR.LANDING_VSPEED DESC, PR.DATE DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, eqType);
			_ps.setInt(2, MIN_ACARS_CLIENT);
			_ps.setInt(3, FlightReport.OK);
			if (_dayFilter > 0)
				_ps.setInt(4, _dayFilter);
			
			// Add to the cache
			results = new CacheableList<FlightReport>("EQ" + eqType + "$" + _dayFilter);
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
		StringBuilder buf = new StringBuilder("SELECT P.EQTYPE, COUNT(P.ID) AS CNT FROM PIREPS P, ACARS_PIREPS APR "
				+ "LEFT JOIN acars.FLIGHTS F ON (APR.ACARS_ID=F.ID) LEFT JOIN acars.CONS C ON (F.CON_ID=C.ID) WHERE "
				+ "(P.ID=APR.ID) AND (P.STATUS=?) AND (C.CLIENT_BUILD >= ?) AND (APR.LANDING_VSPEED < 0)");
		if (_dayFilter > 0)
			buf.append(" AND (P.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		buf.append(" GROUP BY P.EQTYPE HAVING (CNT >= ?) ORDER BY CNT DESC");
		
		try {
			int pos = 0;
			prepareStatement(buf.toString());
			_ps.setInt(++pos, FlightReport.OK);
			_ps.setInt(++pos, MIN_ACARS_CLIENT);
			if (_dayFilter > 0)
				_ps.setInt(++pos, _dayFilter);
			
			_ps.setInt(++pos, minLegs);

			// Execute the query
			Collection<String> results = new LinkedHashSet<String>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(rs.getString(1));

			// Clean up and return
			rs.close();
			_ps.close();
			return new ArrayList<String>(results);
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
			ResultSet rs = _ps.executeQuery();
			int results = rs.next() ? rs.getInt(1) : 0;

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}