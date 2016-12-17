// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;
import java.time.Instant;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.util.StringUtils;

/**
 * A Data Access Object for Oceanic Routes.
 * @author Luke
 * @version 7.2
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
     * @param routeType the route Type
     * @param vd the validity date
     * @return the OceanicRoute
     * @throws DAOException if a JDBC error occurs
     */
    public OceanicNOTAM get(OceanicTrackInfo.Type routeType, java.time.Instant vd) throws DAOException {
    	try {
    		prepareStatementWithoutLimits("SELECT * FROM common.OCEANIC WHERE (ROUTETYPE=?) AND (VALID_DATE=DATE(?)) LIMIT 1");
    		_ps.setInt(1, routeType.ordinal());
    		_ps.setTimestamp(2, createTimestamp(vd));
    		
    		// Get the results and return the first element
    		List<OceanicNOTAM> results = execute();
    		return (results.size() == 0) ? null : results.get(0);
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
    
    /*
     * Helper method to load Oceanic Route data.
     */
    private List<OceanicNOTAM> execute() throws SQLException {
    	List<OceanicNOTAM> results = new ArrayList<OceanicNOTAM>();
        try (ResultSet rs = _ps.executeQuery()) {
        	while (rs.next()) {
        		OceanicTrackInfo.Type rType = OceanicTrackInfo.Type.values()[rs.getInt(1)];
        		OceanicNOTAM or = new OceanicNOTAM(rType, expandDate(rs.getDate(2)));
        		or.setSource(rs.getString(3));
        		or.setRoute(rs.getString(4));
        		results.add(or);
        	}
        }	
        	
        _ps.close();
        return results;
    }
    
    /**
     * Returns the dates for which oceanic track waypoints are available.
     * @param routeType the route type
     * @return a Collection of {@link java.util.Date} objects
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<java.time.Instant> getOceanicTrackDates(OceanicTrackInfo.Type routeType) throws DAOException {
    	try {
    		prepareStatement("SELECT DISTINCT VALID_DATE FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?) ORDER BY VALID_DATE DESC");
    		_ps.setInt(1, routeType.ordinal());
    		Collection<java.time.Instant> results = new LinkedHashSet<java.time.Instant>();
    		try (ResultSet rs = _ps.executeQuery()) {
    			while (rs.next())
    				results.add(Instant.ofEpochMilli(rs.getDate(1).getTime()));
    		}
    		
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
     * @return a {@link DailyOceanicTracks} bean
     * @throws DAOException if a JDBC error occurs
     */
    public DailyOceanicTracks getOceanicTracks(OceanicTrackInfo.Type routeType, java.time.Instant dt) throws DAOException {
    	
    	// Build the SQL statement
    	StringBuilder sqlBuf = new StringBuilder("SELECT * FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?) AND ");
    	if (dt == null)
    		sqlBuf.append("(VALID_DATE = (SELECT MAX(VALID_DATE) FROM common.OCEANIC_ROUTES WHERE (ROUTETYPE=?)))");
    	else
    		sqlBuf.append("(VALID_DATE=DATE(?))");
    	
    	sqlBuf.append(" ORDER BY TRACK, SEQ");
    	
    	try {
    		prepareStatementWithoutLimits(sqlBuf.toString());
    		_ps.setInt(1, routeType.ordinal());
    		if (dt != null)
    			_ps.setTimestamp(2, createTimestamp(dt));
    		else
    			_ps.setInt(2, routeType.ordinal());
    		
    		// Execute the query
    		OceanicTrack trk = null;
    		Collection<OceanicTrack> tmpResults = new ArrayList<OceanicTrack>();
    		try (ResultSet rs = _ps.executeQuery()) {
    			while (rs.next()) {
    				String newTrack = rs.getString(3);
    				if ((trk == null) || (!newTrack.equals(trk.getTrack()))) {
    					trk = new OceanicTrack(routeType, newTrack);
    					trk.setDate(rs.getTimestamp(2).toInstant());
    					tmpResults.add(trk);
    				}
    			
    				// Get the waypoint placeholder - we'll populate later
    				Intersection np = new Intersection(rs.getString(5), rs.getDouble(6), rs.getDouble(7));
    				trk.addWaypoint(np);
    			}
    		}
    		
    		_ps.close();
    		
    		// Now populate the waypoint data using a different prepared statement
    		DailyOceanicTracks results = new DailyOceanicTracks(routeType, dt);
    		for (OceanicTrack ot : tmpResults) {
    			OceanicTrack nt = new OceanicTrack(ot.getType(), ot.getTrack());
    			for (NavigationDataBean np : ot.getWaypoints()) {
    				NavigationDataMap ndmap = get(np.getCode());
    				NavigationDataBean nd = ndmap.get(np.getCode(), np);
    				if (nd != null) {
						try {
							NavigationDataBean nd2 = (NavigationDataBean) nd.clone();
							if (StringUtils.isEmpty(nd2.getAirway()))
								nd2.setAirway(nt.getCode());
							
							nt.addWaypoint(nd2);
						} catch (CloneNotSupportedException cnse) {
							// 	empty
						}
    				}
    			}
    			
    			results.addTrack(nt);
    		}

    		return results;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
}