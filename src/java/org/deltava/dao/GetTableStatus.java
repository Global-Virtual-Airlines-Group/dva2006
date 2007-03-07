// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.stats.TableInfo;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load mySQL table status.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetTableStatus extends DAO implements CachingDAO {
	
	private static final Cache<CacheableSet<String>> _cache = new AgingCache<CacheableSet<String>>(2);
	
	/**
     * Creates the DAO using a JDBC connection.
     * @param c the JDBC connection to use
     */
    public GetTableStatus(Connection c) {
        super(c);
    }
    
	/**
	 * Returns the number of cache hits.
	 * @return the number of hits
	 */
	public int getRequests() {
		return _cache.getRequests();
	}
	
	/**
	 * Returns the number of cache requests.
	 * @return the number of requests
	 */
	public int getHits() {
		return _cache.getHits();
	}
    
    /**
     * Get the database table status.
     * @param dbName the database name
     * @return a List of TableInfo beans
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<TableInfo> getStatus(String dbName) throws DAOException {
       
    	dbName = formatDBName(dbName);
        try {
            prepareStatementWithoutLimits("SHOW TABLE STATUS FROM " + dbName);
            
            // Execute the query
            List<TableInfo> results = new ArrayList<TableInfo>();
            ResultSet rs = _ps.executeQuery();
            
            // Iterate through the results
            while (rs.next()) {
                TableInfo info = new TableInfo(dbName + "." + rs.getString(1));
                info.setRows(rs.getInt(5));
                info.setSize(rs.getLong(7));
                info.setIndexSize(rs.getLong(9));
                results.add(info);
            }
            
            // Close the result set
            rs.close();
            _ps.close();
            
            // Return results
            return results;
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Returns the names of all tables present in a database.
     * @param dbName the database name
     * @return a Collection of table names
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<String> getTableNames(String dbName) throws DAOException {
    	
    	// Check the cache
    	dbName = formatDBName(dbName);
    	CacheableSet<String> results = _cache.get(dbName);
    	if (results != null)
    		return results;
    	
    	// Do the query
    	try {
    		prepareStatementWithoutLimits("SHOW TABLES FROM " + dbName);
    		ResultSet rs = _ps.executeQuery();
    		
    		// Iterate through the results
    		results = new CacheableSet<String>(dbName);
    		while (rs.next())
    			results.add(rs.getString(1).toUpperCase());
    		
    		// Clean up and save in the cache
    		rs.close();
    		_ps.close();
    		_cache.add(results);
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    	
    	return results;
    }
}