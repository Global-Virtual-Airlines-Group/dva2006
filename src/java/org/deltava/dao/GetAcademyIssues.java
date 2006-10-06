// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object to load Flight Academy Issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetAcademyIssues extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAcademyIssues(Connection c) {
		super(c);
	}

	/**
	 * Returns a particular Flight Academy Issue.
	 * @param id the database ID
	 * @return an Issue bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Issue getIssue(int id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT I.*, COUNT(IC.ID) FROM ACADEMY_ISSUES I LEFT JOIN ACADEMY_ISSUECOMMENTS IC ON "
					+ "(I.ID=IC.ID) WHERE (I.ID=?) GROUP BY I.ID ORDER BY I.CREATED_ON");
			_ps.setInt(1, id);
			
			// Do the query and return the first result
			List<Issue> results = execute();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Academy Issues.
	 * @return a Collection of Issue beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Issue> getAll() throws DAOException {
		try {
			prepareStatement("SELECT I.*, COUNT(IC.ID) FROM ACADEMY_ISSUES I LEFT JOIN ACADEMY_ISSUECOMMENTS IC ON "
					+ "(I.ID=IC.ID) GROUP BY I.ID ORDER BY I.CREATED_ON");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all active Flight Academy Issues.
	 * @return a Collection of Issue beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Issue> getActive() throws DAOException {
		try {
			prepareStatement("SELECT I.*, COUNT(IC.ID) FROM ACADEMY_ISSUES I LEFT JOIN ACADEMY_ISSUECOMMENTS IC ON "
					+ "(I.ID=IC.ID) WHERE (I.STATUS=?) GROUP BY I.ID ORDER BY I.CREATED_ON");
			_ps.setInt(1, Issue.OPEN);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse Issue result sets.
	 */
	private List<Issue> execute() throws SQLException {
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasCount = (rs.getMetaData().getColumnCount() > 6);
		
		// Load results
		List<Issue> results = new ArrayList<Issue>();
		while (rs.next()) {
			Issue i = new Issue(rs.getString(5));
			i.setID(rs.getInt(1));
			i.setAuthorID(rs.getInt(2));
			i.setCreatedOn(rs.getTimestamp(3));
			i.setStatus(rs.getInt(4));
			i.setBody(rs.getString(6));
			if (hasCount)
				i.setCommentCount(rs.getInt(7));
			
			// Add to results
			results.add(i);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}