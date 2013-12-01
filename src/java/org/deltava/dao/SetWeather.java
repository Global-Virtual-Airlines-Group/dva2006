// Copyright 2009, 2011, 2012, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.wx.*;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to save weather data in the database.
 * @author Luke
 * @version 5.2
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
		try {
			prepareStatementWithoutLimits("REPLACE INTO common.METARS (AIRPORT, DATE, ILS, DATA) VALUES (?, ?, ?, ?)");
			_ps.setString(1, m.getCode());
			_ps.setTimestamp(2, createTimestamp(m.getDate()));
			_ps.setInt(3, m.getILS().ordinal());
			_ps.setString(4, m.getData());
			executeUpdate(1);
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
		try {
			prepareStatementWithoutLimits("REPLACE INTO common.TAFS (AIRPORT, DATE, AMENDED, DATA) VALUES (?, ?, ?, ?)");
			_ps.setString(1, t.getCode());
			_ps.setTimestamp(2, createTimestamp(t.getDate()));
			_ps.setBoolean(3, t.getAmended());
			_ps.setString(4, t.getData());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("TAF", t.cacheKey());
		}
	}
	
	/**
	 * Writes wind data to the database.
	 * @param effDate the effective date of the data
	 * @param data the WindData
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(java.util.Date effDate, java.util.Collection<WindData> data) throws DAOException {
		try {
			prepareStatement("REPLACE INTO common.WINDS (DATE, LAT, LNG, MB, ALT, WSPD, WDIR) VALUES (?, ?, ?, ?, ?, ?, ?)");
			_ps.setTimestamp(1, createTimestamp(effDate));
			
			int cnt = 0;
			for (WindData wd : data) {
				cnt++;
				_ps.setDouble(2, wd.getLatitude());
				_ps.setDouble(3, wd.getLongitude());
				_ps.setInt(4, wd.getLevel().getPressure());
				_ps.setInt(5, 0);
				_ps.setInt(6, wd.getJetStreamSpeed());
				_ps.setInt(7, wd.getJetStreamDirection());
				_ps.addBatch();
				if (cnt == 128) {
					_ps.executeBatch();
					cnt = 0;
				}
			}

			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges METAR data older than a particular age.
	 * @param age the age in minutes
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purgeMETAR(int age) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.METARS WHERE (DATE < DATE_SUB(NOW(), INTERVAL ? MINUTE))");
			_ps.setInt(1, age);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("METAR");
		}
	}
	
	/**
	 * Purges wind data older than a particular age.
	 * @param age the age in minutes
	 * @param lvl the pressure level
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purgeWinds(int age, PressureLevel lvl) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.WINDS WHERE (DATE < DATE_SUB(NOW(), INTERVAL ? MINUTE)) AND (MB=?)");
			_ps.setInt(1, age);
			_ps.setInt(2, lvl.getPressure());
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges TAF data older than a particular age.
	 * @param age the age in minutes
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purgeTAF(int age) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.TAFS WHERE (DATE < DATE_SUB(NOW(), INTERVAL ? MINUTE))");
			_ps.setInt(1, age);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("TAF");
		}
	}
}