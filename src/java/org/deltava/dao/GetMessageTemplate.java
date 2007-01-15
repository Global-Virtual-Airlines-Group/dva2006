// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.util.cache.*;
import org.deltava.beans.system.MessageTemplate;

/**
 * A Data Access Object to retrieve e-Mail message templates.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetMessageTemplate extends DAO {

	private static final Logger log = Logger.getLogger(GetMessageTemplate.class);
	static Cache _cache = new AgingCache(4);

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

		// Check the cache
		MessageTemplate result = (MessageTemplate) _cache.get(name);
		if (result != null)
			return result;

		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM MSG_TEMPLATES WHERE (UPPER(NAME)=?)");
			_ps.setString(1, name.toUpperCase());

			// Get the results, if we get back a null, log a warning, otherwise update the cache
			List results = execute();
			setQueryMax(0);
			if (results.isEmpty()) {
				log.warn("Cannot load Message Template " + name);
			} else {
				result = (MessageTemplate) results.get(0);
				_cache.add(result);
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		return result;
	}

	/**
	 * Returns all Message Templates.
	 * @return a List of MessageTemplate beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<MessageTemplate> getAll() throws DAOException {
		try {
			prepareStatement("SELECT * FROM MSG_TEMPLATES");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse the result set.
	 */
	private List<MessageTemplate> execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<MessageTemplate> results = new ArrayList<MessageTemplate>();
		while (rs.next()) {
			MessageTemplate mt = new MessageTemplate(rs.getString(1));
			mt.setSubject(rs.getString(2));
			mt.setDescription(rs.getString(3));
			mt.setBody(rs.getString(4));
			mt.setIsHTML(rs.getBoolean(5));

			// Add to results
			results.add(mt);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}