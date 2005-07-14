// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.system.MessageTemplate;

/**
 * A Data Access Object to retrieve e-Mail message templates.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetMessageTemplate extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetMessageTemplate(Connection c) {
		super(c);
	}

	/**
	 * Returns a particular Message Template.
	 * @param name the Template Name
	 * @return a MessageTemplate bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public MessageTemplate get(String name) throws DAOException {
		try {
			prepareStatement("SELECT * FROM MSG_TEMPLATES WHERE (UPPER(NAME)=?)");
			_ps.setString(1, name.toUpperCase());
			
			// Get the results, if empty return null
			List results = execute();
			return results.isEmpty() ? null : (MessageTemplate) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Message Templates.
	 * @return a List of MessageTemplate beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getAll() throws DAOException {
		try {
			prepareStatement("SELECT * FROM MSG_TEMPLATES");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Message Template from the database.
	 * @param name the template name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(String name) throws DAOException {
		try {
			prepareStatement("DELETE FROM MSG_TEMPLATES WHERE (UPPER(NAME)=?)");
			_ps.setString(1, name.toUpperCase());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse the result set.
	 */
	private List execute() throws SQLException {
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		
		// Iterate through the results
		List results = new ArrayList();
		while (rs.next()) {
			MessageTemplate mt = new MessageTemplate(rs.getString(1));
			mt.setSubject(rs.getString(2));
			mt.setDescription(rs.getString(3));
			mt.setBody(rs.getString(4));
			
			// Add to results
			results.add(mt);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}