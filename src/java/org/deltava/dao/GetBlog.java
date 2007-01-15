// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.blog.*;

/**
 * A Data Access Object to load blog entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetBlog extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetBlog(Connection c) {
		super(c);
	}
	
	/**
	 * Loads a blog entry.
	 * @param id the database ID
	 * @return a blog Entry bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Entry get(int id) throws DAOException {
		try {
			prepareStatement("SELECT * FROM BLOG WHERE (ID=?)");
			_ps.setInt(1, id);
			_ps.setMaxRows(1);
			
			// Execute the query, return null if empty
			List<Entry> results = execute();
			if (results.isEmpty())
				return null;
			
			// Get the entry
			Entry e = results.get(0);
			loadComments(e);
			return e;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the latest blog entries.
	 * @param id the author's database ID
	 * @param showPrivate TRUE if all entries should be displayed, otherwise FALSE
	 * @return a Collection of Entry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Entry> getLatest(int id, boolean showPrivate) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT E.*, COUNT(C.CREATED) FROM BLOG E LEFT JOIN "
				+ "BLOGCOMMENTS C ON (E.ID=C.ID) WHERE (E.AUTHOR_ID=?) ");
		if (!showPrivate)
			sqlBuf.append("AND (E.PRIVATE=?) ");
		
		sqlBuf.append("GROUP BY E.ID ORDER BY E.CREATED DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, id);
			if (!showPrivate)
				_ps.setBoolean(2, false);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all blog entries.
	 * @return a Collection of Entry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Entry> getAll() throws DAOException {
		try {
			prepareStatement("SELECT E.*, COUNT(C.CREATED) FROM BLOG E LEFT JOIN BLOGCOMMENTS C ON "
					+ "(E.ID=C.ID) GROUP BY E.ID ORDER BY E.CREATED DESC");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the ID of all blog entry authors.
	 * @param showPrivate TRUE if private entries should be included, otherwise FALSE
	 * @return a Collection of database IDs 
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getAuthors(boolean showPrivate) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT AUTHOR_ID FROM BLOG");
		if (!showPrivate)
			sqlBuf.append(" WHERE (PRIVATE=?)");
		
		try {
			prepareStatement(sqlBuf.toString());
			if (!showPrivate)
				_ps.setBoolean(1, false);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			Collection<Integer> results = new HashSet<Integer>();
			while (rs.next())
				results.add(new Integer(rs.getInt(1)));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse result sets.
	 */
	private List<Entry> execute() throws SQLException {
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasCounts = (rs.getMetaData().getColumnCount() > 7);
		
		// Iterate through the results
		List<Entry> results = new ArrayList<Entry>();
		while (rs.next()) {
			Entry e = new Entry(rs.getString(4));
			e.setID(rs.getInt(1));
			e.setDate(rs.getTimestamp(2));
			e.setAuthorID(rs.getInt(3));
			e.setPrivate(rs.getBoolean(5));
			e.setLocked(rs.getBoolean(6));
			e.setBody(rs.getString(7));
			if (hasCounts)
				e.setSize(rs.getInt(8));
			
			// Add to results
			results.add(e);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to load comments for a blog entry.
	 */
	private void loadComments(Entry e) throws SQLException {
		
		// Prepare the statement
		prepareStatementWithoutLimits("SELECT *, INET_NTOA(REMOTE_ADDR) FROM BLOGCOMMENTS WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Comment c = new Comment(rs.getString(3), rs.getString(7));
			c.setID(rs.getInt(1));
			c.setDate(rs.getTimestamp(2));
			c.setEmail(rs.getString(4));
			c.setRemoteHost(rs.getString(6));
			c.setRemoteAddr(rs.getString(8));
			e.addComment(c);
		}
		
		// Clean up
		rs.close();
		_ps.close();
	}
}