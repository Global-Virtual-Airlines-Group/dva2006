// Copyright 2010, 2014, 2015, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.stats.Accomplishment;
import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to save Accomplishment profiles.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepare("INSERT INTO ACCOMPLISHMENTS (NAME, UNIT, VAL, COLOR, CHOICES, ACTIVE, ALWAYS_SHOW) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPCATE KEY UPDATE "
				+ "NAME=VALUES(NAME), UNIT=VALUES(UNIT), VAL=VALUES(VAL), COLOR=VALUES(COLOR), CHOICES=VALUES(CHOICES), ACTIVE=VALUES(ACTIVE), ALWAYS_SHOW=VALUES(ALWAYS_SHOW)")) {
				ps.setString(1, a.getName());
				ps.setInt(2, a.getUnit().ordinal());
				ps.setInt(3, a.getValue());
				ps.setInt(4, a.getColor());
				ps.setString(5, a.getChoices().isEmpty() ? null : StringUtils.listConcat(a.getChoices(), ","));
				ps.setBoolean(6, a.getActive());
				ps.setBoolean(7, a.getAlwaysDisplay());
				executeUpdate(ps, 1);
			}
			
			if (a.getID() == 0) a.setID(getNewID());
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO PILOT_ACCOMPLISHMENTS (PILOT_ID, AC_ID, DATE) VALUES (?, ?, ?)")) {
			ps.setInt(1, pilotID);
			ps.setInt(2, a.getID());
			ps.setTimestamp(3, createTimestamp(dt));
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("DELETE FROM PILOT_ACCOMPLISHMENTS WHERE (PILOT_ID=?) AND (AC_ID=?)")) {
			ps.setInt(1, pilotID);
			ps.setInt(2, a.getID());
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepare("DELETE FROM ACCOMPLISHMENTS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Accomplishments");
		}
	}
}