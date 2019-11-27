// Copyright 2008, 2011, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.Livery;
import org.deltava.beans.schedule.Airline;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read ACARS multi-player livery profiles.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT NAME, ISDEFAULT FROM acars.LIVERIES WHERE (AIRLINE=?) AND (LIVERY=?) LIMIT 1")) {
			ps.setString(1, a.getCode());
			ps.setString(2, code);
			
			// Execute the query
			Livery l = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					l = new Livery(a, code);
					l.setDescription(rs.getString(1));
					l.setDefault(rs.getBoolean(2));
				}
			}
			
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
		
		try (PreparedStatement ps = prepare(buf.toString())) {
			if (a != null)
				ps.setString(1, a.getCode());
			
			// Execute the query
			Collection<Livery> results = new ArrayList<Livery>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Livery l = new Livery(SystemData.getAirline(rs.getString(1)), rs.getString(2));
					l.setDescription(rs.getString(3));
					l.setDefault(rs.getBoolean(4));
					results.add(l);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}