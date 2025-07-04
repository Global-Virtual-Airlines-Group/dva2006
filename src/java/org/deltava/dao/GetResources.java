// Copyright 2006, 2007, 2009, 2010, 2011, 2019, 2020, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.fleet.Resource;

/**
 * A Data Access Object to load Web Resource data.
 * @author Luke
 * @version 10.4
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM RESOURCES WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			return execute(ps).stream().findFirst().orElse(null);
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
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			if (catName != null) ps.setString(1, catName);
			return execute(ps);
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
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setBoolean(1, true);
			ps.setInt(2, id);
			if (catName != null) ps.setString(3, catName);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Resources associated with a particular Flight Academy certification.
	 * @param dbName the database name
	 * @param certName the Certification name
	 * @return a Collection of Resource beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Resource> getByCertification(String dbName, String certName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT R.* FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".RESOURCES R, exams.CERTRSRCS CR WHERE (R.ID=CR.ID) AND (CR.CERT=?)");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, certName);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse result sets.
	 */
	private static List<Resource> execute(PreparedStatement ps) throws SQLException {
		List<Resource> results = new ArrayList<Resource>();
		try (ResultSet rs = ps.executeQuery()) {
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
		}
		
		return results;
	}
}