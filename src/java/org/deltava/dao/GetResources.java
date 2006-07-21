// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.fleet.Resource;

/**
 * A Data Access Object to load Web Resource data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetResources extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetResources(Connection c) {
		super(c);
	}

	/**
	 * Returns a Web Resource.
	 * @param id the database ID
	 * @return the Resource bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Resource get(int id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM RESOURCES WHERE (ID=?)");
			_ps.setInt(1, id);
			
			// Get first result
			List<Resource> results = execute();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Web Resources.
	 * @param orderBy the sorting SQL fragment
	 * @return a Collection of Resource beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Resource> getAll(String orderBy) throws DAOException {
		try {
			prepareStatement("SELECT * FROM RESOURCES ORDER BY " + orderBy);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Web Resources available to a particular user.
	 * @param orderBy the sorting SQL fragment
	 * @param id the user's database ID
	 * @return a Collection of Resource beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Resource> getAll(String orderBy, int id) throws DAOException {
		try {
			prepareStatement("SELECT * FROM RESOURCES WHERE (ISPUBLIC=?) OR (AUTHOR=?) ORDER BY " + orderBy);
			_ps.setBoolean(1, true);
			_ps.setInt(2, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse result sets.
	 */
	private List<Resource> execute() throws SQLException {
	
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		List<Resource> results = new ArrayList<Resource>();
		while (rs.next()) {
			Resource r = new Resource(rs.getString(2));
			r.setID(rs.getInt(1));
			r.setDescription(rs.getString(3));
			r.setCreatedOn(expandDate(rs.getDate(4)));
			r.setAuthorID(rs.getInt(5));
			r.setLastUpdateID(rs.getInt(6));
			r.setHits(rs.getInt(7));
			r.setPublic(rs.getBoolean(8));
			
			// Add to results
			results.add(r);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}