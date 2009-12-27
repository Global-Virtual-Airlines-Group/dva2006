// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.stats.TableInfo;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load mySQL table status.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class GetTableStatus extends DAO implements CachingDAO {
	
	private static final Cache<CacheableCollection<TableInfo>> _cache = new ExpiringCache<CacheableCollection<TableInfo>>(12, 1800);
	
	/**
     * Initializes the Data Access Object.
     * @param c the JDBC connection to use
     */
    public GetTableStatus(Connection c) {
        super(c);
    }

    /**
     * Returns the cache status.
     */
	public CacheInfo getCacheInfo() {
		return new CacheInfo(_cache);
	}
    
    /**
     * Get the database table status.
     * @param dbName the database name
     * @return a List of TableInfo beans
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<TableInfo> getStatus(String dbName) throws DAOException {
    	
    	// Check the cache
    	dbName = formatDBName(dbName);
    	CacheableCollection<TableInfo> results = _cache.get(dbName);
    	if (results != null)
    		return results;
    	
        try {
            prepareStatementWithoutLimits("SHOW TABLE STATUS FROM " + dbName);
            
            // Execute the query
            results = new CacheableList<TableInfo>(dbName);
            ResultSet rs = _ps.executeQuery();
            while (rs.next()) {
                TableInfo info = new TableInfo(dbName + "." + rs.getString(1));
                info.setRows(rs.getLong(5));
                info.setSize(rs.getLong(7));
                info.setIndexSize(rs.getLong(9));
                results.add(info);
            }
            
            // Close the result set
            rs.close();
            _ps.close();
        } catch (SQLException se) {
            throw new DAOException(se);
        }

        // Add to cache and return
        _cache.add(results);
        return results;
    }
}