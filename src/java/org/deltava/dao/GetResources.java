// Copyright 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.fleet.Resource;

/**
 * A Data Access Object to load Web Resource data.
 * @author Luke
 * @version 2.7
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
			prepareStatementWithoutLimits("SELECT * FROM RESOURCES WHERE (ID=?) LIMIT 1");
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
	 * @param catName the category name, or null
	 * @param orderBy the sorting SQL fragment
	 * @return a Collection of Resource beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Resource> getAll(String catName, String orderBy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM RESOURCES ");
		if (catName != null)
			sqlBuf.append("WHERE (CATEGORY=?) ");
		
		sqlBuf.append("ORDER BY ");
		sqlBuf.append(orderBy);
		
		try {
			prepareStatement(sqlBuf.toString());
			if (catName != null)
				_ps.setString(1, catName);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Web Resources available to a particular user.
	 * @param catName the category name, or null
	 * @param id the user's database ID
	 * @param orderBy the sorting SQL fragment
	 * @return a Collection of Resource beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Resource> getAll(String catName, int id, String orderBy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM RESOURCES WHERE ((ISPUBLIC=?) OR (AUTHOR=?)) ");
		if (catName != null)
			sqlBuf.append("AND (CATEGORY=?) ");
		
		sqlBuf.append("ORDER BY ");
		sqlBuf.append(orderBy);
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setBoolean(1, true);
			_ps.setInt(2, id);
			if (catName != null)
				_ps.setString(3, catName);
			
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
			r.setTitle(rs.getString(3));
			r.setDescription(rs.getString(4));
			r.setCreatedOn(expandDate(rs.getDate(5)));
			r.setCategory(rs.getString(6));
			r.setAuthorID(rs.getInt(7));
			r.setLastUpdateID(rs.getInt(8));
			r.setHits(rs.getInt(9));
			r.setPublic(rs.getBoolean(10));
			results.add(r);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}