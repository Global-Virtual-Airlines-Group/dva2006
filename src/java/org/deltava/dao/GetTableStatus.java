package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.TableInfo;

/**
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class GetTableStatus extends DAO {

    /**
     * Creates the DAO using a JDBC connection.
     * @param c the JDBC connection to use
     */
    public GetTableStatus(Connection c) {
        super(c);
    }
    
    /**
     * Get the database table status. This is for mySQL only.
     * @param dbName the database name
     * @return a List of TableInfo beans
     * @throws DAOException if a JDBC error occurs
     */
    public List execute(String dbName) throws DAOException {
       
        try {
            prepareStatementWithoutLimits("SHOW TABLE STATUS FROM " + dbName);
            
            // Execute the query
            List results = new ArrayList();
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
}