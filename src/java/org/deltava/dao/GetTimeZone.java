// Copyright 2004, 2005, 2006, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.TZInfo;

/**
 * A Data Access Object for loading Time Zones.
 * @author Luke
 * @version 3.2
 * @since 1.0
 */

public class GetTimeZone extends DAO {

    /**
     * Initializes the Data Access Object.
     * @param c the JDBC connection to use
     */
    public GetTimeZone(Connection c) {
        super(c);
    }

    /**
     * Intiailizes all Time Zones from the database.
     * @return the number of Time Zones loaded
     * @throws DAOException if a JDBC error occurs
     */
    public int initAll() throws DAOException {
        try {
            prepareStatementWithoutLimits("SELECT CODE, NAME, ABBR FROM common.TZ");
            
            // Execute the query
            int rowsLoaded = 0;
            ResultSet rs = _ps.executeQuery();
            while (rs.next()) {
            	String id = rs.getString(1);
                TZInfo.init(id, rs.getString(2), rs.getString(3));
                rowsLoaded++;
            }
            
            // Clean up after ourselves
            rs.close();
            _ps.close();
            return rowsLoaded;
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
}