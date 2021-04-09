// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.econ.*;

/**
 * A Data Access Object to update Elite status level definitions.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class SetElite extends EliteDAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetElite(Connection c) {
		super(c);
	}

	/**
	 * Updates an Elite level in the database.
	 * @param lvl the EliteLevel
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(EliteLevel lvl) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO ELITE_LEVELS (NAME, YR, LEGS, DISTANCE, POINTS, BONUS, COLOR, TARGET_PCT, VISIBLE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
			ps.setString(1, lvl.getName());
			ps.setInt(2, lvl.getYear());
			ps.setInt(3, lvl.getLegs());
			ps.setInt(4, lvl.getDistance());
			ps.setInt(5, lvl.getPoints());
			ps.setInt(6, Math.round(lvl.getBonusFactor() * 100));
			ps.setInt(7, lvl.getColor());
			ps.setInt(8, lvl.getTargetPercentile());
			ps.setBoolean(9, lvl.getIsVisible());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			_lvlCache.remove(lvl.cacheKey());
		}
	}
	
	/**
	 * Updates a Pilot's Elite Status in the database.
	 * @param es an EliteStatus bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(EliteStatus es) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO ELITE_STATUS (PILOT_ID, NAME, YR, CREATED, UPD_REASON) VALUES (?, ?, ?, ?, ?)")) {
			ps.setInt(1, es.getID());
			ps.setString(2, es.getLevel().getName());
			ps.setInt(3, es.getLevel().getYear());
			ps.setTimestamp(4, createTimestamp(es.getEffectiveOn()));
			ps.setInt(5, es.getUpgradeReason().ordinal());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			_stCache.remove(es.cacheKey());
		}
	}
	
	/**
	 * Deletes an Elite level from the database.
	 * @param name the level name
	 * @param year the level year
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(String name, int year) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM ELITE_LEVELS WHERE (NAME=?) AND (YR=?)")) {
			ps.setString(1, name);
			ps.setInt(2, year);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			_lvlCache.remove(new EliteLevel(year, name).cacheKey());
		}
	}
	
	/**
	 * Resets a Pilot's Elite status for a particular year.
	 * @param pilotID the Pilot's database ID
	 * @param year the year
	 * @param includeRollover
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clear(int pilotID, int year, boolean includeRollover) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM ELITE_STATUS WHERE (PILOT_ID=?) AND (YR=?)");
		if (!includeRollover)
			sqlBuf.append(" AND (UPD REASON<>?) AND (UPD_REASON<>?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, pilotID);
			ps.setInt(2, year);
			if (!includeRollover) {
				ps.setInt(3, UpgradeReason.ROLLOVER.ordinal());
				ps.setInt(4, UpgradeReason.DOWNGRADE.ordinal());
			}
			
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			_stCache.remove(EliteStatus.generateKey(year, pilotID));
		}
	}
}