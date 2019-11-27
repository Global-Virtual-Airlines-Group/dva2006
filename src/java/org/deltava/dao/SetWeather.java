// Copyright 2009, 2011, 2012, 2013, 2014, 2015, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.wx.*;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to save weather data in the database.
 * @author Luke
 * @version 9.0
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
	 * Writes a METAR  bean to the database.
	 * @param m the METAR
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(METAR m) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.METARS (AIRPORT, DATE, ILS, LOC, DATA) VALUES (?, ?, ?, ST_PointFromText(?,?), ?)")) {
			ps.setString(1, m.getCode());
			ps.setTimestamp(2, createTimestamp(m.getDate()));
			ps.setInt(3, m.getILS().ordinal());
			ps.setString(4, formatLocation(m));
			ps.setInt(5, WGS84_SRID);
			ps.setString(6, m.getData());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("METAR", m.cacheKey());
		}
	}
	
	/**
	 * Writes a TAF bean to the database.
	 * @param t the TAF
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(TAF t) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.TAFS (AIRPORT, DATE, AMENDED, DATA) VALUES (?, ?, ?, ?)")) {
			ps.setString(1, t.getCode());
			ps.setTimestamp(2, createTimestamp(t.getDate()));
			ps.setBoolean(3, t.getAmended());
			ps.setString(4, t.getData());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("TAF", t.cacheKey());
		}
	}
	
	/**
	 * Purges METAR data older than a particular age.
	 * @param age the age in minutes
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purgeMETAR(int age) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.METARS WHERE (DATE < DATE_SUB(NOW(), INTERVAL ? MINUTE))")) {
			ps.setInt(1, age);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("METAR");
		}
	}
	
	/**
	 * Purges TAF data older than a particular age.
	 * @param age the age in minutes
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purgeTAF(int age) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.TAFS WHERE (DATE < DATE_SUB(NOW(), INTERVAL ? MINUTE))")) {
			ps.setInt(1, age);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("TAF");
		}
	}
}