// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2019, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.stats.TableInfo;
import org.deltava.beans.stats.TableInfo.*;

import org.deltava.util.EnumUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to load mySQL table status.
 * @author Luke
 * @version 12.5
 * @since 1.0
 */

public class GetTableStatus extends DAO {
	
	private static final Cache<CacheableCollection<TableInfo>> _cache = CacheManager.getCollection(TableInfo.class, "TableStats");
	
	/**
     * Initializes the Data Access Object.
     * @param c the JDBC connection to use
     */
    public GetTableStatus(Connection c) {
        super(c);
    }

    /**
     * Get the database table status.
     * @param dbName the database name
     * @return a List of TableInfo beans
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<TableInfo> getStatus(String dbName) throws DAOException {
    	
    	// Check the cache
    	String db = formatDBName(dbName);
    	CacheableCollection<TableInfo> results = _cache.get(db);
    	if (results != null)
    		return results.clone();
    	
    	try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.TABLE_INFO WHERE (DB=?)")) {
    		ps.setString(1, db);
            try (ResultSet rs = ps.executeQuery()) {
            	results = new CacheableList<TableInfo>(db);
            	while (rs.next()) {
            		TableInfo info = new TableInfo(db + "." + rs.getString(2));
            		info.setRowFormat(EnumUtils.parse(RowFormat.class, rs.getString(3), RowFormat.DYNAMIC));
            		info.setCompression(EnumUtils.parse(TableCompression.class, rs.getString(4), TableCompression.NONE));
            		info.setRows(rs.getLong(5));
            		info.setSize(rs.getLong(6));
            		info.setIndexSize(rs.getLong(7));
            		info.setFileSize(rs.getLong(8));
            		info.setDiskSize(rs.getLong(9));
            		results.add(info);
            	}
            }
        } catch (SQLException se) {
            throw new DAOException(se);
        }

        _cache.add(results);
        return results.clone();
    }
}