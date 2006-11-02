// Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.beans.TZInfo;

/**
 * A Data Access Object for loading Time Zones.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetTimeZone extends DAO {

    private static final Logger log = Logger.getLogger(GetTimeZone.class);
    
    /**
     * Initializes the Data Access Object.
     * @param c the JDBC connection to use
     */
    public GetTimeZone(Connection c) {
        super(c);
    }

    /**
     * Intiailizes all Time Zones from the database.
     * @throws DAOException if a JDBC error occurs
     */
    public void initAll() throws DAOException {
        try {
            prepareStatementWithoutLimits("SELECT CODE, NAME, ABBR FROM common.TZ");
            
            // Execute the query
            ResultSet rs = _ps.executeQuery();
            while (rs.next()) {
            	String id = rs.getString(1);
                TZInfo.init(id, rs.getString(2), rs.getString(3));
            }
            
            // Clean up after ourselves
            rs.close();
            _ps.close();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
        
        // Log map size
        log.info("Loaded " + TZInfo.getAll().size() + " time zones");
    }
}