// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.schedule.Airport;

/**
 * A Data Access Object to calculate average taxi times. 
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public class GetACARSTaxiTimes extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetACARSTaxiTimes(Connection c) {
		super(c);
	}

	/**
	 * Returns the average outbound taxi time for a particular Airport.
	 * @param a the Airport
	 * @param db the database name
	 * @return the average taxi time, in seconds
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getTaxiOutTime(Airport a, String db) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT AVG(TIMESTAMPDIFF(SECOND, TAXI_TIME, TAKEOFF_TIME)) AS TX_TKO FROM ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".ACARS_PIREPS AP, ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".PIREPS P WHERE (AP.ID=P.ID) AND (AP.TAXI_TIME < AP.TAKEOFF_TIME) AND (AP.TAKEOFF_TIME < DATE_ADD(AP.TAXI_TIME, INTERVAL 2 HOUR)) AND (P.AIRPORT_D=?)");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, a.getIATA());
			
			int result = -1;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					result = rs.getInt(1);
			}
			
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the average inbound taxi time for a particular Airport.
	 * @param a the Airport
	 * @param db the database name
	 * @return the average taxi time, in seconds
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getTaxiInTime(Airport a, String db) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT AVG(TIMESTAMPDIFF(SECOND, LANDING_TIME, END_TIME)) AS TX_LND FROM ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".ACARS_PIREPS AP, ");
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".PIREPS P WHERE (AP.ID=P.ID) AND (AP.LANDING_TIME < AP.END_TIME) AND (AP.END_TIME < DATE_ADD(AP.LANDING_TIME, INTERVAL 2 HOUR)) AND (P.AIRPORT_A=?)");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, a.getIATA());
			
			int result = -1;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					result = rs.getInt(1);
			}
			
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}