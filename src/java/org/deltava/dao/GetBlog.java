// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
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
			setQueryMax(1);
			prepareStatement("SELECT * FROM BLOG WHERE (E.ID=?)");
			_ps.setInt(1, id);
			
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
		try {
			prepareStatement("SELECT E.*, COUNT(C.CREATED) FROM BLOG E LEFT JOIN BLOGCOMMENTS C ON "
					+ "(E.ID=C.ID) WHERE (E.AUTHOR_ID=?) AND (E.PRIVATE=?) GROUP BY E.ID ORDER BY E.CREATED DESC");
			_ps.setInt(1, id);
			_ps.setBoolean(2, showPrivate);
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
	 * Helper method to parse result sets.
	 */
	private List<Entry> execute() throws SQLException {
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		
		// Iterate through the results
		List<Entry> results = new ArrayList<Entry>();
		while (rs.next()) {
			Entry e = new Entry(rs.getString(4));
			e.setID(rs.getInt(1));
			e.setDate(rs.getTimestamp(2));
			e.setAuthorID(rs.getInt(3));
			e.setPrivate(rs.getBoolean(5));
			e.setBody(rs.getString(6));
			
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
		prepareStatementWithoutLimits("SELECT * FROM BLOGCOMMENTS WHERE (ID=?)");
		_ps.setInt(1, e.getID());
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Comment c = new Comment(rs.getString(3), rs.getString(5));
			c.setID(rs.getInt(1));
			c.setDate(rs.getTimestamp(2));
			c.setEmail(rs.getString(4));
			e.addComment(c);
		}
		
		// Clean up
		rs.close();
		_ps.close();
	}
}