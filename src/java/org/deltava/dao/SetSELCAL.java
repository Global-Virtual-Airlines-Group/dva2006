// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.schedule.SelectCall;

/**
 * A Data Access Object to write aircraft SELCAL data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetSELCAL extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetSELCAL(Connection c) {
		super(c);
	}

	/**
	 * Adds or updates an aircraft SELCAL code.
	 * @param sc the SelectCall bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(SelectCall sc) throws DAOException {
		try {
			prepareStatement("REPLACE INTO SELCAL (CODE, AIRCRAFT, EQTYPE, PILOT_ID, RESERVE_DATE) VALUES "
					+ "(?, ?, ?, ?, ?)");
			_ps.setString(1, sc.getCode());
			_ps.setString(2, sc.getAircraftCode());
			_ps.setString(3, sc.getEquipmentType());
			_ps.setInt(4, sc.getReservedBy());
			_ps.setTimestamp(5, createTimestamp(sc.getReservedOn()));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Reserves an aircraft SELCAL code for a particular Pilot.
	 * @param code the SELCAL code
	 * @param pilotID the database ID of the Pilot
	 * @throws DAOException if a JDBC error occurs
	 */
	public void reserve(String code, int pilotID) throws DAOException {
		try {
			prepareStatement("UPDATE SELCAL SET PILOT_ID=?, RESERVE_DATE=NOW() WHERE (CODE=?)");
			_ps.setInt(1, pilotID);
			_ps.setString(2, code);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Releases an aircraft SELCAL code reservation.
	 * @param code the SELCAL code
	 * @throws DAOException if a JDBC error occurs
	 */
	public void free(String code) throws DAOException {
		try {
			prepareStatement("UPDATE SELCAL SET PILOT_ID=0, RESERVE_DATE=NULL WHERE (CODE=?)");
			_ps.setString(1, code);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an aircraft SELCAL code from the database.
	 * @param code the SELCAL code
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if code is null
	 */
	public void delete(String code) throws DAOException {
		try {
			prepareStatement("DELETE FROM SELCAL WHERE (CODE=?)");
			_ps.setString(1, code.toUpperCase());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Frees aircraft SELCAL codes reserved for a set interval.
	 * @param days the interval in days
	 * @return the number of codes freed up
	 * @throws DAOException if a JDBC error occurs
	 */
	public int free(int days) throws DAOException {
		try {
			prepareStatement("UPDATE SELCAL SET PILOT_ID=0, RESERVE_DATE=NULL WHERE (RESERVE_DATE < "
					+ "DATE_SUB(NOW(), INTERVAL ? DAY))");
			_ps.setInt(1, days);
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}