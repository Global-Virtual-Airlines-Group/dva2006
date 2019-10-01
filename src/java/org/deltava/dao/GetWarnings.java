// Copyright 2011, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.Warning;

/**
 * A Data Access Object to load ACARS content warnings.
 * @author Luke
 * @version 8.7
 * @since 4.0
 */

public class GetWarnings extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetWarnings(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the warnings for a Pilot.
	 * @param pilotID the Pilot database ID
	 * @return a Collection of Warning beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Warning> get(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT * FROM acars.WARNINGS WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			
			// Execute the query
			Collection<Warning> results = new ArrayList<Warning>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					Warning w = new Warning(rs.getInt(1), rs.getInt(2), rs.getInt(4));
					w.setDate(rs.getTimestamp(3).toInstant());
					results.add(w);
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}