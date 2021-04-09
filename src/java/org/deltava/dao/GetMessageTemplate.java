// Copyright 2005, 2007, 2009, 2011, 2012, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import org.deltava.beans.system.*;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve e-Mail message templates.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class GetMessageTemplate extends DAO {

	private static final Logger log = Logger.getLogger(GetMessageTemplate.class);
	private static final Cache<MessageTemplate> _cache = CacheManager.get(MessageTemplate.class, "MessageTemplate");

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
		MessageTemplate result = _cache.get(name);
		if (result != null) return result;
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
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT MT.*, GROUP_CONCAT(MTA.ACTION,?) AS ACTS FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".MSG_TEMPLATES MT LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".MSG_TEMPLATE_ACTIONS MTA ON (MT.NAME=MTA.NAME) WHERE (MT.NAME=?) GROUP BY MT.NAME LIMIT 1");
		
		MessageTemplate result = null;
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, ",");
			ps.setString(2, name);

			// Get the results, if we get back a null, log a warning, otherwise update the cache
			List<MessageTemplate> results = execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT MT.*, GROUP_CONCAT(MTA.ACTION,?) AS ACTS FROM MSG_TEMPLATES MT LEFT JOIN MSG_TEMPLATE_ACTIONS MTA ON (MT.NAME=MTA.NAME) GROUP BY MT.NAME")) {
			ps.setString(1, ",");
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse the result set.
	 */
	private static List<MessageTemplate> execute(PreparedStatement ps) throws SQLException {
		List<MessageTemplate> results = new ArrayList<MessageTemplate>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				MessageTemplate mt = new MessageTemplate(rs.getString(1));
				mt.setSubject(rs.getString(2));
				mt.setDescription(rs.getString(3));
				mt.setNotifyContext(rs.getString(4));
				mt.setBody(rs.getString(5));
				mt.setIsHTML(rs.getBoolean(6));
				mt.setNotificationTTL(rs.getInt(7));
				Collection<String> acts = StringUtils.split(rs.getString(8), ",");
				if (acts != null)
					mt.setActionTypes(acts.stream().map(o -> NotifyActionType.values()[StringUtils.parse(o, 0)]).collect(Collectors.toList()));
					
				results.add(mt);
			}
		}

		return results;
	}
}