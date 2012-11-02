// Copyright 2009, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.flight.ILSCategory;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.wx.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load weather data from the database.
 * @author Luke
 * @version 5.0
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
	 * Retrieves the METAR for a particular Airport.
	 * @param a the ICAOAirport
	 * @return a METAR object, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public METAR getMETAR(ICAOAirport a) throws DAOException {
		return (a == null) ? null : getMETAR(a.getICAO());
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
			prepareStatementWithoutLimits("SELECT M.DATE, M.DATA, M.ILS, ND.LATITUDE, ND.LONGITUDE, ND.ALTITUDE FROM "
				+ "common.METARS M LEFT JOIN common.NAVDATA ND ON (M.AIRPORT=ND.CODE) AND (ND.ITEMTYPE=?) WHERE "
				+ "(M.AIRPORT=?) LIMIT 1");
			_ps.setInt(1, Navaid.AIRPORT.ordinal());
			_ps.setString(2, code);
			
			// Load the METAR
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next()) {
					String data = rs.getString(2);
					m = MetarParser.parse(data);
					m.setDate(rs.getTimestamp(1));
					m.setILS(ILSCategory.values()[rs.getInt(3)]);
					m.setData(data);
					AirportLocation loc = new AirportLocation(rs.getDouble(4), rs.getDouble(5));
					loc.setAltitude(rs.getInt(6));
					loc.setCode(code);
					m.setAirport(loc);
					_wxCache.add(m);
				}
			}				
			
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
			_ps.setInt(1, Navaid.AIRPORT.ordinal());
			_ps.setString(2, code);

			// Load the TAF
			try (ResultSet rs = _ps.executeQuery()) {
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
			}
			
			_ps.close();
			return t;
		} catch (SQLException se) {
			throw new DAOException(se);
		}		
	}

	/**
	 * Returns Airports with ILS conditions. 
	 * @param ilscat the ILSCategory
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getILSAirports(ILSCategory ilscat) throws DAOException {
		try {
			prepareStatement("SELECT M.AIRPORT FROM common.METARS M, common.AIRPORTS A, SCHEDULE S WHERE (M.AIRPORT=A.ICAO) "
				+ "AND ((A.IATA=S.AIRPORT_D) OR (A.IATA=S.AIRPORT_A)) AND (M.ILS>=?)");
			_ps.setInt(1, ilscat.ordinal());
			
			Collection<Airport> results = new LinkedHashSet<Airport>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					Airport a = SystemData.getAirport(rs.getString(1));
					if (a != null)
						results.add(a);
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}