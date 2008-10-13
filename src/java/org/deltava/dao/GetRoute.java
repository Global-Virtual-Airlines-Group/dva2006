// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.navdata.Intersection;
import org.deltava.beans.schedule.*;
import org.deltava.comparators.AirportComparator;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object for Preferred/Oceanic Routes.
 * @author Luke
 * @version 2.2
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
     * Return a list of preferred routes from a particular airport.
     * @param srcAirport the source airport IATA code
     * @param dstAirport the destination airport IATA code, or null
     * @return a List of Preferred routes
     * @throws DAOException if a JDBC error occurs
     * @deprecated
     */
    @Deprecated
    public List<PreferredRoute> getRoutes(String srcAirport, String dstAirport) throws DAOException {
        
       // Build the SQL statement
       StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ROUTES WHERE (AIRPORT_D=?)");
       if (dstAirport != null) sqlBuf.append(" AND (AIRPORT_A=?)");
       sqlBuf.append(" ORDER BY AIRPORT_A");
       
        try {
            // Init the prepared statement
            prepareStatement(sqlBuf.toString());
            _ps.setString(1, srcAirport);
            if (dstAirport != null)
               _ps.setString(2, dstAirport);
            
            // Get the airports map
            Map airports = (Map) SystemData.getObject("airports");
            
            // Execute the query
            ResultSet rs = _ps.executeQuery();
            List<PreferredRoute> results = new ArrayList<PreferredRoute>();
            
            // Iterate through the results
            while (rs.next()) {
                Airport ad = (Airport) airports.get(rs.getString(2));
                Airport aa = (Airport) airports.get(rs.getString(3));
                PreferredRoute pr = new PreferredRoute(ad, aa);
                pr.setID(rs.getInt(1));
                pr.setARTCC(rs.getString(4));
                pr.setRoute(rs.getString(5));
                
                // Add to the results
                results.add(pr);
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
     * Returns a list of all destinations with a preferred Route from a particular Airport.
     * @param aCode the source Airport IATA code
     * @return a List of Airport beans
     * @throws DAOException
     */
    public Collection<Airport> getRouteDestinations(String aCode) throws DAOException {
       try {
          prepareStatementWithoutLimits("SELECT DISTINCT AIRPORT_A FROM ROUTES WHERE (AIRPORT_D=?) ORDER BY AIRPORT_A");
          _ps.setString(1, aCode);
          
          // Iterate through the result set
          Set<Airport> results = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
          ResultSet rs = _ps.executeQuery();
          while (rs.next()) {
             Airport a = SystemData.getAirport(rs.getString(1));
             if (a != null)
                results.add(a);
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
     * Returns all Airports with a Preferred Route entry.
     * @return a List of Airports
     * @throws DAOException if a JDBC error occurs
     */
    public List<Airport> getAirports() throws DAOException {
        try {
            prepareStatementWithoutLimits("SELECT DISTINCT(AIRPORT_D) FROM ROUTES ORDER BY AIRPORT_D");

            // Get the airport info map
            Map airports = (Map) SystemData.getObject("airports");
            
            // Execute the query
            ResultSet rs = _ps.executeQuery();
            List<Airport> results = new ArrayList<Airport>();
            
            // Iterate through the results
            while (rs.next()) {
                Airport a = (Airport) airports.get(rs.getString(1));
                results.add(a);
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
}