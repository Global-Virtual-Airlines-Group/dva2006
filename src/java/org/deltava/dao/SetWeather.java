// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.wx.*;

/**
 * A Data Access Object to save weather data in the database.
 * @author Luke
 * @version 2.7
 * @since 2.7
 */

public class SetWeather extends WeatherDAO {

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
			prepareStatementWithoutLimits("REPLACE INTO common.METARS (AIRPORT, DATE, DATA) VALUES (?, ?, ?)");
			_ps.setString(1, m.getCode());
			_ps.setTimestamp(2, createTimestamp(m.getDate()));
			_ps.setString(3, m.getData());
			executeUpdate(1);
			_wxCache.remove(m.getCode());
		} catch (SQLException se) {
			throw new DAOException(se);
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
			_tafCache.remove(t.getCode());
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
			_wxCache.clear();
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
			_tafCache.clear();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}