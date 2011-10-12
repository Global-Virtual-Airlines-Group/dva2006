// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.schedule.Country;

/**
 * A Data Access Object to load ISO-3316 country codes.
 * @author Luke
 * @version 4.1
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
		try {
			prepareStatementWithoutLimits("SELECT CODE, NAME, CONTINENT FROM common.COUNTRY");
			int rowsLoaded = 0;
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Country.init(rs.getString(1), rs.getString(2), rs.getString(3));
				rowsLoaded++;
			}
			
			rs.close();
			_ps.close();
			return rowsLoaded;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}