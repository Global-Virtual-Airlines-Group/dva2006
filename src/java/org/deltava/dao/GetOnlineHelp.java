// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.help.OnlineHelpEntry;

import org.deltava.util.cache.*;

/**
 * A Data Access Object for Online Help entries.
 * @author Luke
 * @version 4.1
 * @since 1.0
 */

public class GetOnlineHelp extends DAO implements CachingDAO {
	
	private static final Cache<OnlineHelpEntry> _cache = new AgingCache<OnlineHelpEntry>(16);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetOnlineHelp(Connection c) {
		super(c);
	}

	@Override
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_cache);
	}
	
	/**
	 * Returns a particular Online Help Entry.
	 * @param id the entry title
	 * @return a HelpEntry bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public OnlineHelpEntry get(String id) throws DAOException {
		
		// Check the cache
		OnlineHelpEntry entry = _cache.get(id);
		if (entry != null)
			return entry;
		
		try {
			prepareStatementWithoutLimits("SELECT * FROM HELP WHERE (ID=?) LIMIT 1");
			_ps.setString(1, id);

			// Execute the query, return first result
			List<OnlineHelpEntry> results = executeHelp();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Online Help Entries.
	 * @return a Collection of OnlineHelpEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<OnlineHelpEntry> getOnlineHelp() throws DAOException {
		try {
			prepareStatement("SELECT * FROM HELP ORDER BY ID");
			return executeHelp();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse the result set.
	 */
	private List<OnlineHelpEntry> executeHelp() throws SQLException {
		List<OnlineHelpEntry> results = new ArrayList<OnlineHelpEntry>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				OnlineHelpEntry entry = new OnlineHelpEntry(rs.getString(1), rs.getString(2));
				results.add(entry);
				_cache.add(entry);
			}
		}

		_ps.close();
		return results;
	}
}