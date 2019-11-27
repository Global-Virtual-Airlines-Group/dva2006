// Copyright 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.AIRSPACE (ID, NAME, COUNTRY, TYPE, EXCLUSION, MIN_ALT, MAX_ALT, LATITUDE, LONGITUDE, DATA) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?, ?))")) {
			ps.setString(1, a.getID());
			ps.setString(2, a.getName());
			ps.setString(3, a.getCountry().getCode());
			ps.setInt(4, a.getType().ordinal());
			ps.setBoolean(5, a.isExclusion());
			ps.setInt(6, a.getMinAltitude());
			ps.setInt(7, a.getMaxAltitude());
			ps.setDouble(8, a.getLatitude());
			ps.setDouble(9,  a.getLongitude());
			WKTWriter ww = new WKTWriter();
			ps.setString(10, ww.write(geo));
			ps.setInt(11, WGS84_SRID);
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.AIRSPACE WHERE (COUNTRY=?)")) {
			ps.setString(1, c.getCode());
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}