// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.navdata.Intersection;
import org.deltava.beans.schedule.*;

/**
 * A Data Access Object for Preferred/Oceanic Routes.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class GetRoute extends DAO {

    /**
     * Initializes the DAO with the specified JDBC connection.
     * @param c the JDBC connection to use
     */
    public GetRoute(Connection c) {
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
    public OceanicRoute get(int routeType, java.util.Date vd) throws DAOException {
    	try {
    		setQueryMax(1);
    		prepareStatement("SELECT * FROM common.OCEANIC WHERE (ROUTETYPE=?) AND (VALID_DATE=?)");
    		_ps.setInt(1, routeType);
    		_ps.setTimestamp(2, createTimestamp(vd));
    		
    		// Get the results and return the first element
    		List results = execute();
    		setQueryMax(0);
    		return (results.size() == 0) ? null : (OceanicRoute) results.get(0);
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
            OceanicNOTAM or = new OceanicNOTAM(rs.getInt(1), expandDate(rs.getDate(2)));
            or.setSource(rs.getString(3));
            or.setRoute(rs.getString(4));
            
            // Add to results
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
    public Collection<java.util.Date> getOceanicTrackDates(int routeType) throws DAOException {
    	try {
    		prepareStatement("SELECT DISTINCT VALID_DATE FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?) "
    				+ "ORDER BY VALID_DATE DESC");
    		_ps.setInt(1, routeType);
    		
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
     * @return a Map of {@link OceanicWaypoints} beans, keyed by track code
     * @throws DAOException if a JDBC error occurs
     */
    public Map<String, OceanicWaypoints> getOceanicTrakcs(int routeType, java.util.Date dt) throws DAOException {
    	
    	// Build the SQL statement
    	StringBuilder buf = new StringBuilder("SELECT * FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?) AND ");
    	if (dt == null)
    		buf.append("(VALID_DATE = (SELECT MAX(VALID_DATE) FROM common.OCEANIC_ROUTES))");
    	else
    		buf.append("(VALID_DATE=DATE(?))");
    	
    	buf.append(" ORDER BY TRACK, SEQ");
    	
    	try {
    		prepareStatementWithoutLimits(buf.toString());
    		_ps.setInt(1, routeType);
    		if (dt != null)
    			_ps.setTimestamp(2, createTimestamp(dt));
    		
    		// Execute the query
    		OceanicWaypoints wp = null;
    		Map<String, OceanicWaypoints> results = new TreeMap<String, OceanicWaypoints>();
    		ResultSet rs = _ps.executeQuery();
    		while (rs.next()) {
    			String newTrack = rs.getString(3);
    			if ((wp == null) || (!newTrack.equals(wp.getTrack()))) {
    				wp = new OceanicWaypoints(routeType, dt);
    				wp.setDate(rs.getTimestamp(2));
    				wp.setTrack(newTrack);
    				results.put(newTrack, wp);
    			}
    			
    			// Add the waypoint
    			Intersection i = new Intersection(rs.getString(5), rs.getDouble(6), rs.getDouble(7));
    			i.setAirway(OceanicRoute.TYPES[routeType] + wp.getTrack());
    			wp.addWaypoint(i);
    		}
    		
    		// Clean up and return
    		rs.close();
    		_ps.close();
    		return results;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
    
    /**
     * Retruns the track code for a particular oceanic track given a start/end waypoint.
     * @param type the track type
     * @param startWP the starting waypoint code
     * @param endWP the ending waypoint code
     * @param dt the validity date
     * @return the track code, or null if not found
     * @throws DAOException if a JDBC error occurs
     */
    public String getOceanicTrack(int type, String startWP, String endWP, java.util.Date dt) throws DAOException {
    	try {
    		prepareStatementWithoutLimits("SELECT TRACK FROM OCEANIC_ROUTES WHERE (VALID_DATE=?) "
    			+ "AND ((WAYPOINT=?) OR (WAYPOINT=?)) LIMIT 2");
    		_ps.setDate(1, new java.sql.Date(dt.getTime()));
    		_ps.setString(2, startWP);
    		_ps.setString(3, endWP);
    		
    		// Do the query
    		Collection<String> results = new HashSet<String>();
    		ResultSet rs = _ps.executeQuery();
    		while (rs.next())
    			results.add(rs.getString(1));
    		
    		// Clean up and return
    		rs.close();
    		_ps.close();
    		if (results.size() != 1)
    			return null;
    		
    		return results.iterator().next();
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
}