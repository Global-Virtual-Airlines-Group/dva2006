// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.wx.*;

/**
 * A Data Access Object to load weather data from the database.
 * @author Luke
 * @version 2.7
 * @since 2.7
 */

public class GetWeather extends WeatherDAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetWeather(Connection c) {
		super(c);
	}

	/**
	 * Retrieves an arbitrary weather data object from the database.
	 * @param t the WeatherDataBean Type
	 * @param code the observation station code
	 * @return a WeatherDataBean, or null if none found
	 * @throws DAOException if a JDBC error occurs
	 */
	public WeatherDataBean get(WeatherDataBean.Type t, String code) throws DAOException {
		switch (t) {
			case METAR:
				return getMETAR(code);
			case TAF:
				return getTAF(code);
			default:
				return null;
		}
	}
	
	/**
	 * Retrieves the METAR for a particular observation station.
	 * @param code the observation station code
	 * @return a METAR object, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public METAR getMETAR(String code) throws DAOException {
		
		// Check the cache
		METAR m = _wxCache.get(code);
		if (m != null)
			return m;
		
		try {
			prepareStatementWithoutLimits("SELECT M.DATE, M.DATA, ND.LATITUDE, ND.LONGITUDE FROM common.METARS M LEFT "
				+ "JOIN common.NAVDATA ND ON (M.AIRPORT=ND.CODE) AND (ND.ITEMTYPE=?) WHERE (M.AIRPORT=?) LIMIT 1");
			_ps.setInt(1, NavigationDataBean.AIRPORT);
			_ps.setString(2, code);
			
			// Load the METAR
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				String data = rs.getString(2);
				m = MetarParser.parse(data);
				m.setDate(rs.getTimestamp(1));
				m.setData(data);
				AirportLocation loc = new AirportLocation(rs.getDouble(3), rs.getDouble(4));
				loc.setCode(code);
				m.setAirport(loc);
				_wxCache.add(m);
			}
			
			// Clean up
			rs.close();
			_ps.close();
			return m;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves the TAF for a particular observation station.
	 * @param code the observation station code
	 * @return a TAF object, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TAF getTAF(String code) throws DAOException {
		
		// Check the cache
		TAF t = _tafCache.get(code);
		if (t != null)
			return t;
		
		try {
			prepareStatementWithoutLimits("SELECT T.DATE, T.AMENDED, T.DATA, ND.LATITUDE, ND.LONGITUDE FROM common.TAFS T "
				+ "LEFT JOIN common.NAVDATA ND ON (T.AIRPORT=ND.CODE) AND (ND.ITEMTYPE=?) WHERE (T.AIRPORT=?) LIMIT 1");
			_ps.setInt(1, NavigationDataBean.AIRPORT);
			_ps.setString(2, code);

			// Load the TAF
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				t = new TAF();
				t.setDate(rs.getTimestamp(1));
				t.setAmended(rs.getBoolean(2));
				t.setData(rs.getString(3));
				AirportLocation loc = new AirportLocation(rs.getDouble(4), rs.getDouble(5));
				loc.setCode(code);
				t.setAirport(loc);
				_tafCache.add(t);
			}
			
			// Clean up
			rs.close();
			_ps.close();
			return t;
		} catch (SQLException se) {
			throw new DAOException(se);
		}		
	}
}