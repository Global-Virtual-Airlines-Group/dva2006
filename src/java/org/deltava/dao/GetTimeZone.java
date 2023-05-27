// Copyright 2004, 2005, 2006, 2010, 2011, 2015, 2016, 2018, 2019, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.*;
import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.*;

import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.Geometry;

/**
 * A Data Access Object for loading Time Zones.
 * @author Luke
 * @version 10.6
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
    	try (PreparedStatement ps = prepareWithoutLimits("SELECT Z.CODE, Z.NAME, Z.ABBR, ST_AsWKT(GTZ.DATA) FROM common.TZ Z LEFT JOIN geoip.TZ GTZ ON (Z.CODE=GTZ.NAME)")) {
            try (ResultSet rs = ps.executeQuery()) {
            	WKTReader wr = new WKTReader();
            	while (rs.next()) {
            		String geoWKT = rs.getString(4);
            		if (!StringUtils.isEmpty(geoWKT)) {
            			Collection<GeoLocation> brdr = new ArrayList<GeoLocation>();
            			Geometry geo = wr.read(rs.getString(4));
            			brdr.addAll(GeoUtils.fromGeometry(geo));
            			TZInfo.init(rs.getString(1), rs.getString(2), rs.getString(3), brdr);
            		} else
            			TZInfo.init(rs.getString(1), rs.getString(2), rs.getString(3));
            	}
			}
        } catch (SQLException | ParseException se) {
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
    		try (PreparedStatement ps = prepareWithoutLimits("SELECT NAME FROM geoip.TZ WHERE (ST_Contains(DATA, ST_PointFromText(?,?)) OR ST_Intersects(DATA, ST_PointFromText(?,?)))")) {
    			ps.setString(1, pt);
    			ps.setInt(2, WGS84_SRID);
    			ps.setString(3, pt);
    			ps.setInt(4, WGS84_SRID);
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