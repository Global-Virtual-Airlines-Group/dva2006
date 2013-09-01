// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to retrieve app-specific metadata.
 * @author Luke
 * @version 5.1
 * @since 5.1
 */

public class GetMetadata extends DAO {
	
	private static final Cache<CacheableString> _cache = CacheManager.get(CacheableString.class, "Metadata");

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetMetadata(Connection c) {
		super(c);
	}
	
	/**
	 * Returns a metadata item.
	 * @param key the key
	 * @return the value, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public String get(String key) throws DAOException {
		return get(key, null);
	}

	/**
	 * Returns a metadata item.
	 * @param key the key
	 * @param defaultValue the default value if not found
	 * @return the value
	 * @throws DAOException if a JDBC error occurs
	 */
	public String get(String key, String defaultValue) throws DAOException {
		
		// Check the cache
		CacheableString v = _cache.get(key);
		if (v != null)
			return v.getValue();
		
		try {
			prepareStatementWithoutLimits("SELECT DATA FROM common.METADATA WHERE (ID=?) LIMIT 1");
			_ps.setString(1, key);
			
			String result = defaultValue;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					result = rs.getString(1);
			}
			
			_ps.close();
			_cache.add(new CacheableString(key, result));
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all metadata items whose key begins with a particular prefix.
	 * @param prefix the key prefix
	 * @return a Map of values, by key
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, String> getAll(String prefix) throws DAOException {
		try {
			prepareStatement("SELECT ID, DATA FROM common.METADATA WHERE (ID LIKE ?) ORDER BY ID");
			_ps.setString(1, prefix + ".%");
			
			Map<String, String> results = new HashMap<String, String>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.put(rs.getString(1), rs.getString(2));
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}		
	}
}