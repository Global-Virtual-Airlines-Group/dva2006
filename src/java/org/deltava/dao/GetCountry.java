// Copyright 2010, 2011, 2017, 2018, 2019, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.Continent;
import org.deltava.beans.schedule.Country;

/**
 * A Data Access Object to load ISO-3316 country codes and perform geolocation.
 * @author Luke
 * @version 11.3
 * @since 3.2
 */

public class GetCountry extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetCountry(Connection c) {
		super(c);
	}

	/**
	 * Initializes all country codes.
	 * @return the number of countries loaded
	 * @throws DAOException if a JDBC error occurs
	 */
	public int initAll() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT CODE, NAME, CONTINENT FROM common.COUNTRY")) {
			int rowsLoaded = 0;
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Country.init(rs.getString(1), rs.getString(2), Continent.valueOf(rs.getString(3)));
					rowsLoaded++;
				}
			}
			
			return rowsLoaded;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Determines what country covers a particular point.
	 * @param loc a GeoLocation
	 * @param isAirspace TRUE if we want INTL returned if no match, otherwise FALSE
	 * @return a Country object, or null/INTL if none found
	 * @throws DAOException if a JDBC error occurs
	 * @see Country#INTL
	 */
	public Country find(GeoLocation loc, boolean isAirspace) throws DAOException {

		String id = null;
		try (PreparedStatement ps = prepareWithoutLimits("SELECT CODE FROM common.COUNTRY_GEO WHERE ST_Contains(DATA, ST_PointFromText(?,?)) LIMIT 1")) {
			ps.setString(1, formatLocation(loc));
			ps.setInt(2, WGS84_SRID);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					id = rs.getString(1);
			}

			if (id != null)
				return Country.get(id);
			else if (isAirspace)
				return Country.INTL;

			return null;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}