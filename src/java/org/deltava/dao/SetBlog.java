// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.blog.*;

/**
 * A Data Access Object to write blog entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetBlog extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetBlog(Connection c) {
		super(c);
	}

	/**
	 * Writes a blog entry to the database. This can handle INSERT and UPDATE operations.
	 * @param e the blog Entry bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Entry e) throws DAOException {
		try {
			if (e.getID() == 0) {
				prepareStatement("INSERT INTO BLOG (TITLE, PRIVATE, BODY, CREATED, AUTHOR_ID) VALUES (?, ?, ?, ?, ?)");
				_ps.setTimestamp(4, createTimestamp(e.getDate()));
				_ps.setInt(5, e.getAuthorID());
			} else {
				prepareStatement("UPDATE BLOGS SET TITLE=?, PRIVATE=?, BODY=? WHERE (ID=?)");
				_ps.setInt(4, e.getID());
			}
			
			// Prepare the statement
			_ps.setString(1, e.getTitle());
			_ps.setBoolean(2, e.getPrivate());
			_ps.setString(3, e.getBody());
			
			// Do the update
			executeUpdate(1);
			
			// Get the ID
			if (e.getID() == 0)
				e.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a new blog entry comment to the database.
	 * @param c the Comment bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Comment c) throws DAOException {
		try {
			prepareStatement("INSERT INTO BLOGCOMMENTS (ID, CREATED, AUTHOR, EMAIL, BODY, REMOTE_ADDR, "
					+ "REMOTE_HOST) VALUES (?, ?, ?, ?, ?, ATON(?), ?)");
			_ps.setInt(1, c.getID());
			_ps.setTimestamp(2, createTimestamp(c.getDate()));
			_ps.setString(3, c.getName());
			_ps.setString(4, c.getEmail());
			_ps.setString(5, c.getBody());
			_ps.setString(6, c.getRemoteAddr());
			_ps.setString(7, c.getRemoteHost());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a blog entry and its comments from the database.
	 * @param id the Entry database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM BLOG WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a blog entry comment from the database.
	 * @param c the Comment bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(Comment c) throws DAOException {
		try {
			prepareStatement("DELETE FROM BLOGCOMMENTS WHERE (ID=?) AND (CREATED=?)");
			_ps.setInt(1, c.getID());
			_ps.setTimestamp(2, createTimestamp(c.getDate()));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}