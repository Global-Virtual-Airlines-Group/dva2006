// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.navdata.*;

/**
 * A Data Access Object to update Navigation data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetNavData extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetNavData(Connection c) {
		super(c);
	}

	/**
	 * Writes an entry to the Navigation Data table.
	 * @param ndata the NavigationDataBean to write
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(NavigationDataBean ndata) throws DAOException {
		try {
			// Prepare the statement if not already done
			if (_ps == null)
				prepareStatement("INSERT INTO common.NAVDATA (ITEMTYPE, CODE, LATITUDE, LONGITUDE, FREQ, ALTITUDE, NAME, HDG) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

			_ps.setInt(1, ndata.getType());
			_ps.setString(2, ndata.getCode());
			_ps.setDouble(3, ndata.getLatitude());
			_ps.setDouble(4, ndata.getLongitude());
			if (ndata.getType() == NavigationDataBean.VOR) {
				VOR vor = (VOR) ndata;
				_ps.setString(5, vor.getFrequency());
				_ps.setInt(6, 0);
				_ps.setString(7, vor.getName());
				_ps.setInt(8, 0);
			} else if (ndata.getType() == NavigationDataBean.NDB) {
				NDB ndb = (NDB) ndata;
				_ps.setString(5, ndb.getFrequency());
				_ps.setInt(6, 0);
				_ps.setString(7, ndb.getName());
				_ps.setInt(8, 0);
			} else if (ndata.getType() == NavigationDataBean.AIRPORT) {
				AirportLocation al = (AirportLocation) ndata;
				_ps.setString(5, "-");
				_ps.setInt(6, al.getAltitude());
				_ps.setString(7, al.getName());
				_ps.setInt(8, 0);
			} else if (ndata.getType() == NavigationDataBean.RUNWAY) {
				Runway rwy = (Runway) ndata;
				_ps.setString(5, rwy.getFrequency());
				_ps.setInt(6, rwy.getLength());
				_ps.setString(7, rwy.getName());
				_ps.setInt(8, rwy.getHeading());
			} else {
				_ps.setString(5, "-");
				_ps.setInt(6, 0);
				_ps.setString(7, "-");
				_ps.setInt(8, 0);
			}

			// Write to the database - and don't clear the prepared statement
			_ps.executeUpdate();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an entry from the Navigation Data table.
	 * @param code the object code
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(String code) throws DAOException {
		try {
			prepareStatement("DELETE FROM common.NAVDATA WHERE (CODE=?)");
			_ps.setString(1, code);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Purges all entries from the Navigation Data table.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge() throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.NAVDATA");
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}