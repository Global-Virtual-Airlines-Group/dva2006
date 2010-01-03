// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.TakeoffLanding;

/**
 * A Data Access Objec to load ACARS Takeoff/Landing data. 
 * @author Luke
 * @version 2.8
 * @since 2.8
 */

public class GetACARSTakeoffs extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSTakeoffs(Connection c) {
		super(c);
	}

	/**
	 * Returns the latest ACARS takeoffs and landings.
	 * @return a Collection of TakeoffLanding beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<TakeoffLanding> getLatest() throws DAOException {
		try {
			prepareStatement("SELECT * FROM acars.TOLAND ORDER BY EVENT_TIME DESC");
			
			// Execute the query
			Collection<TakeoffLanding> results = new ArrayList<TakeoffLanding>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				TakeoffLanding tl = new TakeoffLanding(rs.getInt(1), rs.getBoolean(2));
				tl.setDate(rs.getTimestamp(3));
				results.add(tl);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}