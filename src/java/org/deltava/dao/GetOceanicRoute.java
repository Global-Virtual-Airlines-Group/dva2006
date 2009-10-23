// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.util.StringUtils;

/**
 * A Data Access Object for Oceanic Routes.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class GetOceanicRoute extends GetNavAirway {
	
    /**
     * Initializes the DAO with the specified JDBC connection.
     * @param c the JDBC connection to use
     */
    public GetOceanicRoute(Connection c) {
        super(c);
    }

    /**
     * Returns a list of oceanic routes.
     * @return a List of OceanicRoutes
     * @throws DAOException if a JDBC error occurs
     */
    public List<OceanicNOTAM> getOceanic() throws DAOException {
        try {
            prepareStatement("SELECT * FROM common.OCEANIC ORDER BY VALID_DATE DESC");
            return execute();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }

    /**
     * Returns a specific Oceanic Route.
     * @param routeType the route Type code
     * @param vd the validity date
     * @return the OceanicRoute
     * @throws DAOException if a JDBC error occurs
     */
    public OceanicNOTAM get(int routeType, java.util.Date vd) throws DAOException {
    	try {
    		prepareStatementWithoutLimits("SELECT * FROM common.OCEANIC WHERE (ROUTETYPE=?) AND "
    				+ "(VALID_DATE=DATE(?)) LIMIT 1");
    		_ps.setInt(1, routeType);
    		_ps.setTimestamp(2, createTimestamp(vd));
    		
    		// Get the results and return the first element
    		List<OceanicNOTAM> results = execute();
    		return (results.size() == 0) ? null : results.get(0);
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
    
    /**
     * Helper method to load Oceanic Route data.
     */
    private List<OceanicNOTAM> execute() throws SQLException {
        // Execute the query
        ResultSet rs = _ps.executeQuery();
        List<OceanicNOTAM> results = new ArrayList<OceanicNOTAM>();
        
        // Iterate through the results
        while (rs.next()) {
        	OceanicTrackInfo.Type rType = OceanicTrackInfo.Type.values()[rs.getInt(1)];
            OceanicNOTAM or = new OceanicNOTAM(rType, expandDate(rs.getDate(2)));
            or.setSource(rs.getString(3));
            or.setRoute(rs.getString(4));
            results.add(or);
        }
        
        // Clean up and return
        rs.close();
        _ps.close();
        return results;
    }
    
    /**
     * Returns the dates for which oceanic track waypoints are available.
     * @param routeType the route type
     * @return a Collection of {@link java.util.Date} objects
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<java.util.Date> getOceanicTrackDates(OceanicTrackInfo.Type routeType) throws DAOException {
    	try {
    		prepareStatement("SELECT DISTINCT VALID_DATE FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?) "
    				+ "ORDER BY VALID_DATE DESC");
    		_ps.setInt(1, routeType.ordinal());
    		
    		// Execute the query
    		Collection<java.util.Date> results = new LinkedHashSet<java.util.Date>();
    		ResultSet rs = _ps.executeQuery();
    		while (rs.next())
    			results.add(rs.getDate(1));
    		
    		// Clean up and return
    		rs.close();
    		_ps.close();
    		return results;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
    
    /**
     * Returns all of the oceanic route waypoints for a particular date.
     * @param routeType the route type
     * @param dt the date
     * @return a Map of {@link OceanicTrack} beans, keyed by track code
     * @throws DAOException if a JDBC error occurs
     */
    public Map<String, OceanicTrack> getOceanicTracks(OceanicTrackInfo.Type routeType, java.util.Date dt) throws DAOException {
    	
    	// Build the SQL statement
    	StringBuilder sqlBuf = new StringBuilder("SELECT * FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?) AND ");
    	if (dt == null)
    		sqlBuf.append("(VALID_DATE = (SELECT MAX(VALID_DATE) FROM common.OCEANIC_ROUTES))");
    	else
    		sqlBuf.append("(VALID_DATE=DATE(?))");
    	
    	sqlBuf.append(" ORDER BY TRACK, SEQ");
    	
    	try {
    		prepareStatementWithoutLimits(sqlBuf.toString());
    		_ps.setInt(1, routeType.ordinal());
    		if (dt != null)
    			_ps.setTimestamp(2, createTimestamp(dt));
    		
    		// Execute the query
    		OceanicTrack wp = null;
    		Map<String, OceanicTrack> results = new TreeMap<String, OceanicTrack>();
    		Map<String, NavigationDataBean> wps = new HashMap<String, NavigationDataBean>();
    		ResultSet rs = _ps.executeQuery();
    		while (rs.next()) {
    			String newTrack = rs.getString(3);
    			if ((wp == null) || (!newTrack.equals(wp.getTrack()))) {
    				wp = new OceanicTrack(routeType, newTrack);
    				wp.setDate(rs.getTimestamp(2));
    				results.put(wp.getCode(), wp);
    			}
    			
    			// Get the waypoint
    			String code = rs.getString(5);
    			NavigationDataBean nd = wps.get(code);
    			if (nd != null) {
    				StringBuilder buf = new StringBuilder(nd.getAirway());
    				buf.append(", ");
    				buf.append(wp.getCode());
    				nd.setAirway(buf.toString());
    			} else {
    				NavigationDataMap ndmap = get(code);
    				nd = ndmap.get(code, new GeoPosition(rs.getDouble(6), rs.getDouble(7)));
    				if (nd != null) {
    					try {
    						NavigationDataBean nd2 = (NavigationDataBean) nd.clone();
    						if (StringUtils.isEmpty(nd2.getAirway()))
    							nd2.setAirway(wp.getCode());
    					
    						wps.put(code, nd2);
    						nd = nd2;
    					} catch (CloneNotSupportedException cnse) {
    						// empty
    					}
    				}
    			}
    			
    			// Add the waypoint
   				if (nd != null)
   					wp.addWaypoint(nd);
    		}
    		
    		// Clean up and return
    		rs.close();
    		_ps.close();
    		return results;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
}