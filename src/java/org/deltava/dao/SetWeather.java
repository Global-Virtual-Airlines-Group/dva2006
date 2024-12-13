// Copyright 2009, 2011, 2012, 2013, 2014, 2015, 2018, 2019, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Collection;

import org.deltava.beans.wx.*;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to save weather data in the database.
 * @author Luke
 * @version 11.4
 * @since 2.7
 */

public class SetWeather extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetWeather(Connection c) {
		super(c);
	}

	/**
	 * Writes METAR beans to the database.
	 * @param data a Collection of METARs
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeMETAR(Collection<METAR> data) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.METARS (AIRPORT, DATE, ILS, LOC, DATA) VALUES (?, ?, ?, ST_PointFromText(?,?), ?)")) {
			ps.setInt(5, WGS84_SRID);
			for (METAR m : data) {
				ps.setString(1, m.getCode());
				ps.setTimestamp(2, createTimestamp(m.getDate()));
				ps.setInt(3, m.getILS().ordinal());
				ps.setString(4, formatLocation(m));
				ps.setString(6, m.getData());
				ps.addBatch();
			}
			
			executeUpdate(ps, 1, data.size());
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("METAR");
		}
	}
	
	/**
	 * Writes TAF beans to the database.
	 * @param data a Collection of TAFs
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeTAF(Collection<TAF> data) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.TAFS (AIRPORT, DATE, AMENDED, DATA) VALUES (?, ?, ?, ?)")) {
			for (TAF t : data) {
				ps.setString(1, t.getCode());
				ps.setTimestamp(2, createTimestamp(t.getDate()));
				ps.setBoolean(3, t.getAmended());
				ps.setString(4, t.getData());
				ps.addBatch();
			}
			
			executeUpdate(ps, 1, data.size());
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("TAF");
		}
	}
	
	/**
	 * Purges METAR data older than a particular age.
	 * @param age the age in minutes
	 * @return the number of purged entries
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeMETAR(int age) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.METARS WHERE (DATE < DATE_SUB(NOW(), INTERVAL ? MINUTE))")) {
			ps.setInt(1, age);
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("METAR");
		}
	}
	
	/**
	 * Purges TAF data older than a particular age.
	 * @param age the age in minutes
	 * @return the number of purged entries
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeTAF(int age) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.TAFS WHERE (DATE < DATE_SUB(NOW(), INTERVAL ? MINUTE))")) {
			ps.setInt(1, age);
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("TAF");
		}
	}
}