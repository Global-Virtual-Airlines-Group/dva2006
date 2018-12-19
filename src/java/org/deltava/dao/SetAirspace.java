// Copyright 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTWriter;

import org.deltava.beans.navdata.Airspace;
import org.deltava.beans.schedule.Country;

import org.deltava.util.GeoUtils;

/**
 * A Data Access Object to write Airspace boundaries to the database.
 * @author Luke
 * @version 8.5
 * @since 7.3
 */

public class SetAirspace extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAirspace(Connection c) {
		super(c);
	}

	/**
	 * Writes an Airspace bean to the database.
	 * @param a an Airspace bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Airspace a) throws DAOException {
		
		Geometry geo = GeoUtils.toGeometry(a.getBorder());
		try {
			prepareStatementWithoutLimits("REPLACE INTO common.AIRSPACE (ID, NAME, COUNTRY, TYPE, EXCLUSION, MIN_ALT, MAX_ALT, LATITUDE, LONGITUDE, DATA) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?, ?))");
			_ps.setString(1, a.getID());
			_ps.setString(2, a.getName());
			_ps.setString(3, a.getCountry().getCode());
			_ps.setInt(4, a.getType().ordinal());
			_ps.setBoolean(5, a.isExclusion());
			_ps.setInt(6, a.getMinAltitude());
			_ps.setInt(7, a.getMaxAltitude());
			_ps.setDouble(8, a.getLatitude());
			_ps.setDouble(9,  a.getLongitude());
			WKTWriter ww = new WKTWriter();
			_ps.setString(10, ww.write(geo));
			_ps.setInt(11, WGS84_SRID);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes all Airspace boundaries for a particular country from the database.
	 * @param c the Country
	 * @return the number of records purged
	 * @throws DAOException if a JDBC error occurs
	 */
	public int clear(Country c) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.AIRSPACE WHERE (COUNTRY=?)");
			_ps.setString(1, c.getCode());
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}