// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.EquipmentType;
import org.deltava.beans.flight.FlightReport;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to get Flight Reports for Pilot recognition.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public class GetFlightReportRecognition extends GetFlightReports implements CachingDAO {
	
	private static final Cache<CacheableList<FlightReport>> _cache = 
		new ExpiringCache<CacheableList<FlightReport>>(16, 1800);
	
	private static final double RUNWAY_LDG_ZONE_RATIO = 0.375;
	private static final int TD_ZONE = 1500;
	
	private int _dayFilter;

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetFlightReportRecognition(Connection c) {
		super(c);
	}
	
	@Override
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_cache);
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
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, NULL, NULL, APR.* FROM PIREPS PR, ACARS_PIREPS APR, "
			+ "acars.RWYDATA R WHERE (R.ID=APR.ACARS_ID) AND (R.ISTAKEOFF=?) AND (PR.ID=APR.ID) AND "
			+ "(APR.CLIENT_BUILD >= ?) AND (PR.STATUS=?) AND (APR.LANDING_VSPEED < 0) AND "
			+ "((R.DISTANCE/R.LENGTH) <= ?)");
		if (_dayFilter > 0)
			sqlBuf.append(" AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY APR.LANDING_VSPEED DESC, ABS(?-CAST(R.DISTANCE AS SIGNED)), PR.DATE DESC");

		try {
			int pos = 0;
			prepareStatement(sqlBuf.toString());
			_ps.setBoolean(++pos, false);
			_ps.setInt(++pos, FlightReport.MIN_ACARS_CLIENT);
			_ps.setInt(++pos, FlightReport.OK);
			_ps.setDouble(++pos, RUNWAY_LDG_ZONE_RATIO);
			if (_dayFilter > 0)
				_ps.setInt(++pos, _dayFilter);
			_ps.setInt(++pos, TD_ZONE);

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
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, NULL, NULL, APR.* FROM STAFF S LEFT JOIN (PIREPS PR, "
			+ "ACARS_PIREPS APR, acars.RWYDATA R) ON (PR.PILOT_ID=S.ID) WHERE (PR.ID=APR.ID) AND (R.ID=APR.ACARS_ID) "
			+ "AND (APR.CLIENT_BUILD >= ?) AND (PR.STATUS=?) AND (R.ISTAKEOFF=?) AND (APR.LANDING_VSPEED < 0) "
			+ "AND ((R.DISTANCE/R.LENGTH) <= ?)");
		if (_dayFilter > 0)
			sqlBuf.append(" AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY APR.LANDING_VSPEED DESC, ABS(?-CAST(R.DISTANCE AS SIGNED)), PR.DATE DESC");

		try {
			int pos = 0;
			prepareStatement(sqlBuf.toString());
			_ps.setInt(++pos, FlightReport.MIN_ACARS_CLIENT);
			_ps.setInt(++pos, FlightReport.OK);
			_ps.setBoolean(++pos, false);
			_ps.setDouble(++pos, RUNWAY_LDG_ZONE_RATIO);
			if (_dayFilter > 0)
				_ps.setInt(++pos, _dayFilter);
			_ps.setInt(++pos, TD_ZONE);

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
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, NULL, NULL, APR.* FROM PIREPS PR, ACARS_PIREPS APR, "
			+ "acars.RWYDATA R WHERE (PR.ID=APR.ID) AND (PR.EQTYPE=?) AND (APR.ACARS_ID=R.ID) AND (PR.STATUS=?) "
			+ "AND (R.ISTAKEOFF=?) AND (APR.CLIENT_BUILD >= ?) AND (APR.LANDING_VSPEED < 0) AND "
			+ "((R.DISTANCE/R.LENGTH) <= ?)");
		if (_dayFilter > 0)
			sqlBuf.append(" AND (PR.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		sqlBuf.append(" ORDER BY APR.LANDING_VSPEED DESC, ABS(?-CAST(R.DISTANCE AS SIGNED)), PR.DATE DESC");
		
		try {
			int pos = 0;
			prepareStatement(sqlBuf.toString());
			_ps.setString(++pos, eqType);
			_ps.setInt(++pos, FlightReport.OK);
			_ps.setBoolean(++pos, false);
			_ps.setInt(++pos, FlightReport.MIN_ACARS_CLIENT);
			_ps.setDouble(++pos, RUNWAY_LDG_ZONE_RATIO);
			if (_dayFilter > 0)
				_ps.setInt(++pos, _dayFilter);
			_ps.setInt(++pos, TD_ZONE);
			
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
		StringBuilder buf = new StringBuilder("SELECT P.EQTYPE, COUNT(P.ID) AS CNT FROM PIREPS P, ACARS_PIREPS APR, "
			+ "acars.RWYDATA R WHERE (P.ID=APR.ID) AND (APR.ACARS_ID=R.ID) AND (P.STATUS=?) AND (R.ISTAKEOFF=?) "
			+ "AND (APR.CLIENT_BUILD >= ?) AND (APR.LANDING_VSPEED < 0) AND ((R.DISTANCE/R.LENGTH) <= ?)");
		if (_dayFilter > 0)
			buf.append(" AND (P.DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		buf.append(" GROUP BY P.EQTYPE HAVING (CNT >= ?) ORDER BY CNT DESC");
		
		try {
			int pos = 0;
			prepareStatement(buf.toString());
			_ps.setInt(++pos, FlightReport.OK);
			_ps.setBoolean(++pos, false);
			_ps.setInt(++pos, FlightReport.MIN_ACARS_CLIENT);
			_ps.setDouble(++pos, RUNWAY_LDG_ZONE_RATIO);
			if (_dayFilter > 0)
				_ps.setInt(++pos, _dayFilter);
			_ps.setInt(++pos, minLegs);

			// Execute the query
			Collection<String> results = new LinkedHashSet<String>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}

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
	
	/**
	 * Returns the size of the pending Flight Report queue for an Equipment program. 
	 * @param eqType the equipment program name 
	 * @return the number of pending or held PIREPs
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getDisposalQueueSize(String eqType) throws DAOException {
		try {
			prepareStatement("SELECT COUNT(PR.ID) FROM PIREPS PR, EQRATINGS ER WHERE ((PR.STATUS=?) OR (PR.STATUS=?)) "
				+ "AND (PR.EQTYPE=ER.RATED_EQ) AND (ER.RATING_TYPE=?) AND (ER.EQTYPE=?)");
			_ps.setInt(1, FlightReport.HOLD);
			_ps.setInt(2, FlightReport.SUBMITTED);
			_ps.setInt(3, EquipmentType.PRIMARY_RATING);
			_ps.setString(4, eqType);
			
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
}