// Copyright 2004, 2005, 2006, 2010, 2011, 2015, 2016, 2018, 2019, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.*;
import java.util.*;

import org.deltava.beans.*;

/**
 * A Data Access Object for loading Time Zones.
 * @author Luke
 * @version 11.0
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
     * @throws DAOException if a JDBC error occurs
     */
    public void initAll() throws DAOException {
    	try (PreparedStatement ps = prepareWithoutLimits("SELECT CODE, NAME, ABBR FROM common.TZ"); ResultSet rs = ps.executeQuery()) {
           	while (rs.next())
           		TZInfo.init(rs.getString(1), rs.getString(2), rs.getString(3));
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
    		ZoneId tz = null;
    		try (PreparedStatement ps = prepareWithoutLimits("SELECT NAME FROM geoip.TZ WHERE ST_Contains(DATA, ST_PointFromText(?,?))")) {
    			ps.setString(1, pt);
    			ps.setInt(2, WGS84_SRID);
    			try (ResultSet rs = ps.executeQuery()) {
    				if (rs.next()) tz = ZoneId.of(rs.getString(1));
    			}
    		}
    		
    		if (tz == null) return null;
    		
    		// Get zone info
    		final Instant now = Instant.now();
    		boolean hasDST = (tz.getRules().nextTransition(now) != null);
    		
    		// Converrt into our time zone
    		Collection<TZInfo> allTZ = TZInfo.getAll(); final ZoneId z = tz;
    		TZInfo tzi = allTZ.stream().filter(zi -> zi.getZone().getId().equals(z.getId())).findFirst().orElse(null);
    		if (tzi != null)
    			return tzi;
    		
    		for (TZInfo zoneInfo : allTZ) {
    			ZoneId zi = zoneInfo.getZone(); java.time.zone.ZoneRules zr = zi.getRules();
    			if (zr.equals(tz.getRules()))
    				return zoneInfo;
    			
    			if ((zr.getOffset(now) == tz.getRules().getOffset(now)) && (hasDST == (zr.nextTransition(now) != null)))
    				return zoneInfo;
    		}
    		
    		return null;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
}