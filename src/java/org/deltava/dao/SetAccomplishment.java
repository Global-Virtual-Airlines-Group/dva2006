// Copyright 2010, 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.stats.Accomplishment;
import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to save Accomplishment profiles.
 * @author Luke
 * @version 6.1
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
			if (a.getID() != 0) {
				prepareStatementWithoutLimits("UPDATE ACCOMPLISHMENTS SET NAME=?, UNIT=?, VAL=?, COLOR=?, "
						+ "CHOICES=?, ACTIVE=?, ALWAYS_SHOW=? WHERE (ID=?)");
				_ps.setInt(8, a.getID());
			} else
				prepareStatementWithoutLimits("INSERT INTO ACCOMPLISHMENTS (NAME, UNIT, VAL, COLOR, CHOICES, "
						+ "ACTIVE, ALWAYS_SHOW) VALUES (?, ?, ?, ?, ?, ?, ?)");
			
			_ps.setString(1, a.getName());
			_ps.setInt(2, a.getUnit().ordinal());
			_ps.setInt(3, a.getValue());
			_ps.setInt(4, a.getColor());
			_ps.setString(5, a.getChoices().isEmpty() ? null : StringUtils.listConcat(a.getChoices(), ","));
			_ps.setBoolean(6, a.getActive());
			_ps.setBoolean(7, a.getAlwaysDisplay());
			executeUpdate(1);
			if (a.getID() == 0)
				a.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Accomplishments");
		}
	}
	
	/**
	 * Records a Pilot accomplishment.
	 * @param pilotID the Pilot's database ID
	 * @param a the Accomplishment
	 * @param dt the date/time of the accomplishment
	 * @throws DAOException if a JDBC error occurs
	 */
	public void achieve(int pilotID, Accomplishment a, java.time.Instant dt) throws DAOException {
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
	
	/**
	 * Deletes a Pilot Accomplishment record from the database.
	 * @param pilotID the Pilot's datbaase ID
	 * @param a the Accomplishment
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearAchievement(int pilotID, Accomplishment a) throws DAOException {
		try {
			prepareStatement("DELETE FROM PILOT_ACCOMPLISHMENTS WHERE (PILOT_ID=?) AND (AC_ID=?)");
			_ps.setInt(1, pilotID);
			_ps.setInt(2, a.getID());
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an Accomplishment profile from the database.
	 * @param id the Accomplishment database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM ACCOMPLISHMENTS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Accomplishments");
		}
	}
}