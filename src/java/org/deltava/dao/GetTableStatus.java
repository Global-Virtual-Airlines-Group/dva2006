// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.stats.TableInfo;

/**
 * A Data Access Object to load mySQL table status.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetTableStatus extends DAO {
	
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
        try {
            prepareStatementWithoutLimits("SHOW TABLE STATUS FROM " + formatDBName(dbName));
            
            // Execute the query
            List<TableInfo> results = new ArrayList<TableInfo>();
            ResultSet rs = _ps.executeQuery();
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
            return results;
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
}