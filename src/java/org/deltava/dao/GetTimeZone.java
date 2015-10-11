// Copyright 2004, 2005, 2006, 2010, 2011, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

/**
 * A Data Access Object for loading Time Zones.
 * @author Luke
 * @version 6.2
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
            int rowsLoaded = 0;
            try (ResultSet rs = _ps.executeQuery()) {
            	while (rs.next()) {
            		TZInfo.init(rs.getString(1), rs.getString(2), rs.getString(3));
            		rowsLoaded++;
            	}
            }
            
            _ps.close();
            return rowsLoaded;
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Provides the Time Zone for a given geographic location.
     * @param loc the GeoLocation
     * @return a TZInfo object, or null if unknown
     * @throws DAOException if a JDBC error occurs
     */
    public TZInfo locate(GeoLocation loc) throws DAOException {
    	String pt = formatLocation(loc); 
    	try {
    		TimeZone tz = null;
    		prepareStatementWithoutLimits("SELECT NAME FROM geoip.TZ WHERE ST_Contains(DATA, ST_PointFromText(?,?))");
    		_ps.setString(1, pt);
    		_ps.setInt(2, GEO_SRID);
    		try (ResultSet rs = _ps.executeQuery()) {
    			if (rs.next()) tz = TimeZone.getTimeZone(rs.getString(1));
    		}
    		
    		if (tz == null) {
    			prepareStatementWithoutLimits("SELECT NAME FROM geoip.TZ WHERE ST_Intersects(DATA, ST_PointFromText(?,?))");
    			_ps.setString(1, pt);
        		_ps.setInt(2, GEO_SRID);
        		try (ResultSet rs = _ps.executeQuery()) {
        			if (rs.next()) tz = TimeZone.getTimeZone(rs.getString(1));
        		}
    		}
    		
    		if (tz == null)
    			return null;
    		
    		// Converrt into our time zone
    		Collection<TZInfo> allTZ = TZInfo.getAll();
    		for (TZInfo zoneInfo : allTZ) {
    			TimeZone zi = zoneInfo.getTimeZone();
    			if (zi.getID().equals(tz.getID()) || zi.hasSameRules(tz))
    				return zoneInfo;
    			else if ((zi.getRawOffset() == tz.getRawOffset()) && (zi.observesDaylightTime() == tz.observesDaylightTime()))
    				return zoneInfo;
    		}
    		
    		return null;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
}