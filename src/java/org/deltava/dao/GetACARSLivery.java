// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.Livery;
import org.deltava.beans.schedule.Airline;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read ACARS multi-player livery profiles.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class GetACARSLivery extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetACARSLivery(Connection c) {
		super(c);
	}

	/**
	 * Loads a livery profile.
	 * @param a the Airline bean
	 * @param code the livery code
	 * @return a Livery bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if a or code are null
	 */
	public Livery get(Airline a, String code) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT NAME, ISDEFAULT FROM acars.LIVERIES WHERE (AIRLINE=?) "
					+ "AND (LIVERY=?) LIMIT 1");
			_ps.setString(1, a.getCode());
			_ps.setString(2, code);
			
			// Execute the query
			Livery l = null;
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				l = new Livery(a, code);
				l.setDescription(rs.getString(1));
				l.setDefault(rs.getBoolean(2));
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return l;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all liveries for an Airline.
	 * @param a the Airline bean, or null for all Airlines
	 * @return a Collection of Livery beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Livery> get(Airline a) throws DAOException {
	
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT * FROM acars.LIVERIES ");
		if (a != null)
			buf.append("WHERE (AIRLINE=?) ");
		buf.append("ORDER BY AIRLINE, ISDEFAULT DESC, LIVERY");
		
		try {
			prepareStatement(buf.toString());
			if (a != null)
				_ps.setString(1, a.getCode());
			
			// Execute the query
			Collection<Livery> results = new ArrayList<Livery>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Livery l = new Livery(SystemData.getAirline(rs.getString(1)), rs.getString(2));
				l.setDescription(rs.getString(3));
				l.setDefault(rs.getBoolean(4));
				results.add(l);
			}
			
			// Clean up
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}