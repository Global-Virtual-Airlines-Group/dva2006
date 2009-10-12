// Copyright 2005, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.beans.system.MessageTemplate;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve e-Mail message templates.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class GetMessageTemplate extends DAO implements CachingDAO {

	private static final Logger log = Logger.getLogger(GetMessageTemplate.class);
	static Cache<MessageTemplate> _cache = new AgingCache<MessageTemplate>(16);

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetMessageTemplate(Connection c) {
		super(c);
	}
	
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_cache);
	}
	
	/**
	 * Returns a particular Message Template.
	 * @param name the Template Name
	 * @return a MessageTemplate bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public MessageTemplate get(String name) throws DAOException {
		
		// Check the cache
		MessageTemplate result = _cache.get(name);
		if (result != null)
			return result;
		
		return get(SystemData.get("airline.db"), name);
	}

	/**
	 * Returns a particular Message Template.
	 * @param dbName the database name
	 * @param name the Template Name
	 * @return a MessageTemplate bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public MessageTemplate get(String dbName, String name) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".MSG_TEMPLATES WHERE (NAME=?) LIMIT 1");
		
		MessageTemplate result = null;
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, name.toUpperCase());

			// Get the results, if we get back a null, log a warning, otherwise update the cache
			List<MessageTemplate> results = execute();
			if (!results.isEmpty()) {
				result = results.get(0);
				_cache.add(result);
			} else
				log.warn("Cannot load Message Template " + name);
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