// Copyright 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.mvs.Warning;

/**
 * A Data Access Object to load MVS warnings.
 * @author Luke
 * @version 7.0
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
					Warning w = new Warning(rs.getInt(1), rs.getInt(2));
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

	/**
	 * Returns the number of warnings for a Pilot.
	 * @param pilotID the Pilot database ID
	 * @return the number of warnings
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getCount(int pilotID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT COUNT(*) FROM acars.WARNINGS WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			
			// Execute the query
			int cnt = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					cnt = rs.getInt(1);
			}
			
			_ps.close();
			return cnt;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}