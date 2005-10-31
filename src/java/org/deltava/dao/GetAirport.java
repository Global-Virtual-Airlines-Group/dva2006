// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.navdata.NavigationDataBean;

/**
 * A Data Access Object to load Airport data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetAirport extends DAO {

    /**
     * Creates the DAO with a JDBC connection.
     * @param c the JDBC connection to use
     */
    public GetAirport(Connection c) {
        super(c);
    }
    
    /**
     * Returns an airport object by its IATA or ICAO code.
     * @param code the airport IATA or ICAO code
     * @return an Airport object matching the requested code, or null if not found
     * @throws DAOException if a JDBC error occurs
     * @throws NullPointerException if code is null
     */
    public Airport get(String code) throws DAOException {
        
        // Init the prepared statement in such a way that we can search for ICAO or IATA
        try {
            prepareStatement("SELECT A.*, NV.ALTITUDE FROM common.AIRPORTS A, common.NAVDATA NV "
                  + "WHERE (A.ICAO=NV.CODE) AND (NV.ITEMTYPE=?) AND ((A.ICAO=?) OR (A.IATA=?))");
            _ps.setInt(1, NavigationDataBean.AIRPORT);
            _ps.setString(2, code.toUpperCase());
            _ps.setString(3, code.toUpperCase());
            
            ResultSet rs = _ps.executeQuery();
            if (!rs.next())
                return null;
            
            // Create the airport object
            Airport a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
            a.setTZ(rs.getString(3));
            a.setLocation(rs.getDouble(5), rs.getDouble(6));
            a.setAltitude(rs.getInt(7));
            
            // Close JDBC resources
            rs.close();
            _ps.close();
            
            // Init the prepared statement to pull in the airline data
            prepareStatementWithoutLimits("SELECT CODE FROM common.AIRPORT_AIRLINE WHERE (IATA=?)");
            _ps.setString(1, a.getIATA());
            
            // Iterate through the results
            rs = _ps.executeQuery();
            while (rs.next())
                a.addAirlineCode(rs.getString(1));
            
            // Return the airport
            return a;
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Returns all airports served by a particular airline.
     * @param al the Airline to query with
     * @return a List of Airport objects or an empty list if none found
     * @throws DAOException if a JDBC error occurs
     * @throws NullPointerException if al is null
     */
    public List getByAirline(Airline al) throws DAOException {
        try {
            prepareStatement("SELECT A.*, NV.ALTITUDE FROM common.AIRPORTS A, common.AIRPORT_AIRLINE AA, "
                  + "common.NAVDATA NV WHERE (A.IATA=AA.IATA) AND (A.ICAO=NV.CODE) AND (NV.ITEMTYPE=?) "
                  + " AND (AA.CODE=?) ORDER BY A.IATA");
            _ps.setInt(1, NavigationDataBean.AIRPORT);
            _ps.setString(2, al.getCode());
            
            // Execute the query
            List results = new ArrayList();
            ResultSet rs = _ps.executeQuery();
            
            // Iterate through the result set
            while (rs.next()) {
                Airport a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
                a.setTZ(rs.getString(3));
                a.setLocation(rs.getDouble(5), rs.getDouble(6));
                a.setAltitude(rs.getInt(7));
                
                // Add to the results
                results.add(a);
            }
            
            // CLean up and return
            rs.close();
            _ps.close();
            return results;
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Returns all airports.
     * @return a List of Airports
     * @throws DAOException if a JDBC error occurs
     */
    public Map getAll() throws DAOException {
        Map results = new HashMap();
        try {
            prepareStatementWithoutLimits("SELECT A.*, NV.ALTITUDE FROM common.AIRPORTS A, common.NAVDATA NV "
                  + " WHERE (A.ICAO=NV.CODE) AND (NV.ITEMTYPE=?)");
            _ps.setInt(1, NavigationDataBean.AIRPORT);
            
            // Execute the query
            ResultSet rs = _ps.executeQuery();
            
            // Iterate through the results
            while (rs.next()) {
                Airport a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
                a.setTZ(rs.getString(3));
                a.setLocation(rs.getDouble(5), rs.getDouble(6));
                a.setAltitude(rs.getInt(7));
                
                // Save in the map
                results.put(a.getIATA(), a);
                results.put(a.getICAO(), a);
            }
            
            // Clean up the first query
            rs.close();
            _ps.close();
            
            // Load the airlines for each airport and execute the query
            prepareStatementWithoutLimits("SELECT * FROM common.AIRPORT_AIRLINE");
            rs = _ps.executeQuery();
            
            // Iterate through the results
            while (rs.next()) {
                Airport a = (Airport) results.get(rs.getString(2));
                a.addAirlineCode(rs.getString(1));
            }
            
            // Clean up the second query and return results
            rs.close();
            _ps.close();
            return results;
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
}