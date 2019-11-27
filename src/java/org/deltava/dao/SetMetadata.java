// Copyright 2013, 2015, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to write app-specific metadata.
 * @author Luke
 * @version 9.0
 * @since 5.1
 */

public class SetMetadata extends DAO {
	
	private static final Cache<CacheableString> _cache = CacheManager.get(CacheableString.class, "Metadata");

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetMetadata(Connection c) {
		super(c);
	}

	/**
	 * Writes a metadata entry.
	 * @param key the key
	 * @param value the value
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(String key, Object value) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.METADATA (ID, DATA) VALUES (?, ?)")) {
			ps.setString(1, key);
			ps.setString(2, (value == null) ? null : value.toString());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			_cache.remove(key);
		}
	}
	
	/**
	 * Writes a metadata date entry.
	 * @param key the key
	 * @param dt the value
	 * @throws DAOException if a JDBC error occurs
	 * @see GetMetadata#getDate(String)
	 */
	public void write(String key, java.time.Instant dt) throws DAOException {
		write(key, String.valueOf(dt.toEpochMilli() / 1000));
	}
	
	/**
	 * Deletes a metadata entry.
	 * @param key the key
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(String key) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM common.METADATA WHERE (ID=?)")) {
			ps.setString(1, key);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			_cache.remove(key);
		}
	}
}