package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.schedule.Airport;
import org.deltava.beans.schedule.OceanicRoute;
import org.deltava.beans.schedule.PreferredRoute;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object for Preferred/Oceanic Routes.
 * @author Luke
 * @version 1.0
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
     * @return a List of Preferred routes
     * @throws DAOException if a JDBC error occurs
     */
    public List getRoutes(String srcAirport) throws DAOException {
        
        try {
            // Init the prepared statement
            prepareStatement("SELECT * FROM ROUTES WHERE (AIRPORT_D=?) ORDER BY AIRPORT_A");
            _ps.setString(1, srcAirport);
            
            // Get the airports map
            Map airports = (Map) SystemData.getObject("airports");
            
            // Execute the query
            ResultSet rs = _ps.executeQuery();
            List results = new ArrayList();
            
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
     * Returns a list of oceanic routes.
     * @return a List of OceanicRoutes
     * @throws DAOException if a JDBC error occurs
     */
    public List getOceanic() throws DAOException {
        try {
            prepareStatement("SELECT * FROM common.OCEANIC ORDER BY VALID_DATE DESC");
            return execute();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }

    /**
     * Returns a specific Oceanic Route.
     * @param id the database ID
     * @return the OceanicRoute
     * @throws DAOException if a JDBC error occurs
     */
    public OceanicRoute get(int id) throws DAOException {
    	try {
    		// Init the prepared statement
    		prepareStatement("SELECT * FROM common.OCEANIC WHERE (ID=?)");
    		_ps.setInt(1, id);
    		
    		// Get the results and return the first element
    		List results = execute();
    		return (results.size() == 0) ? null : (OceanicRoute) results.get(0);
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }
    
    /**
     * Helper method to load Oceanic Route data.
     */
    private List execute() throws SQLException {
        // Execute the query
        ResultSet rs = _ps.executeQuery();
        List results = new ArrayList();
        
        // Iterate through the results
        while (rs.next()) {
            OceanicRoute or = new OceanicRoute(rs.getInt(2));
            or.setID(rs.getInt(1));
            or.setDate(rs.getTimestamp(3));
            or.setSource(rs.getString(4));
            or.setRoute(rs.getString(5));
            
            // Add to results
            results.add(or);
        }
        
        // Clean up and return
        rs.close();
        _ps.close();
        return results;
    }
    
    /**
     * Returns all Airports with a Preferred Route entry
     * @return a List of Airports
     * @throws DAOException if a JDBC error occurs
     */
    public List getAirports() throws DAOException {
        try {
            prepareStatementWithoutLimits("SELECT DISTINCT(AIRPORT_D) FROM ROUTES ORDER BY AIRPORT_D");

            // Get the airport info map
            Map airports = (Map) SystemData.getObject("airports");
            
            // Execute the query
            ResultSet rs = _ps.executeQuery();
            List results = new ArrayList();
            
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
}