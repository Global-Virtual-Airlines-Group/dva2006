// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.stats.Accomplishment;

/**
 * A Data Access Object to save Accomplishment profiles.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class SetAccomplishment extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAccomplishment(Connection c) {
		super(c);
	}

	/**
	 * Writes an Accomplishment to the database.
	 * @param a the Accomplishment bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Accomplishment a) throws DAOException {
		try {
			GetAccomplishment.invalidate();
			if (a.getID() != 0) {
				prepareStatementWithoutLimits("UPDATE ACCOMPLISHMENTS SET NAME=?, UNIT=?, VAL=?, COLOR=?, ACTIVE=? WHERE (ID=?)");
				_ps.setInt(6, a.getID());
			} else
				prepareStatementWithoutLimits("INSERT INTO ACCOMPLISHMENTS (NAME, UNIT, VAL, COLOR, ACTIVE) VALUES (?, ?, ?, ?, ?)");
			
			_ps.setString(1, a.getName());
			_ps.setInt(2, a.getUnit().getCode());
			_ps.setInt(3, a.getValue());
			_ps.setInt(4, a.getColor());
			_ps.setBoolean(5, a.getActive());
			executeUpdate(1);
			if (a.getID() == 0)
				a.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	public void achieve(int pilotID, Accomplishment a) throws DAOException {
		achieve(pilotID, a, new java.util.Date());
	}
	
	public void achieve(int pilotID, Accomplishment a, java.util.Date dt) throws DAOException {
		try {
			prepareStatement("REPLACE INTO PILOT_ACCOMPLISHMENTS (PILOT_ID, AC_ID, DATE) VALUES (?, ?, ?)");
			_ps.setInt(1, pilotID);
			_ps.setInt(2, a.getID());
			_ps.setTimestamp(3, createTimestamp(dt));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}