// Copyright 2020, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.econ.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to update Elite status level definitions.
 * @author Luke
 * @version 11.1
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
		try (PreparedStatement ps = prepare("INSERT INTO ELITE_LEVELS (NAME, YR, STAT_START, LEGS, DISTANCE, POINTS, BONUS, COLOR, TARGET_PCT, VISIBLE) VALUES (?,?,?,?,?,?,?,?,?,?) AS N ON DUPLICATE KEY UPDATE "
			+ "STAT_START=N.STAT_START, LEGS=N.LEGS, DISTANCE=N.DISTANCE, POINTS=N.POINTS, BONUS=N.BONUS, COLOR=N.COLOR, TARGET_PCT=N.TARGET_PCT, VISIBLE=N.VISIBLE")) {
			ps.setString(1, lvl.getName());
			ps.setInt(2, lvl.getYear());
			ps.setTimestamp(3, createTimestamp(lvl.getStatisticsStartDate()));
			ps.setInt(4, lvl.getLegs());
			ps.setInt(5, lvl.getDistance());
			ps.setInt(6, lvl.getPoints());
			ps.setInt(7, Math.round(lvl.getBonusFactor() * 100));
			ps.setInt(8, lvl.getColor());
			ps.setInt(9, lvl.getTargetPercentile());
			ps.setBoolean(10, lvl.getIsVisible());
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO ELITE_STATUS (PILOT_ID, NAME, YR, CREATED, UPD_REASON) VALUES (?,?,?,?,?)")) {
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
	 * Rolls over flight legs and distance into a new Elite year.
	 * @param yt a YearlyTotal bean with the amounts to be rolled over
	 * @throws DAOException if a JDBC error occurs
	 */
	public void rollover(YearlyTotal yt) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO ELITE_ROLLOVER (ID, YEAR, LEGS, DISTANCE) VALUES (?,?,?,?)")) {
			ps.setInt(1, yt.getID());
			ps.setInt(2, yt.getYear());
			ps.setInt(3, yt.getLegs());
			ps.setInt(4, yt.getDistance());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("EliteYearlyTotal", Integer.valueOf(yt.getID()));
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
			_lvlCache.remove(new EliteLevel(year, name, SystemData.get("airline.code")).cacheKey());
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
			sqlBuf.append(" AND (UPD_REASON<>?) AND (UPD_REASON<>?)");
		
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