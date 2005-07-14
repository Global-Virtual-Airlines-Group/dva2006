//Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import java.sql.Connection;

import org.deltava.beans.schedule.Airport;
import org.deltava.beans.schedule.Chart;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object for Approach Charts.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetChart extends DAO {

    /**
     * Creates the DAO with a JDBC connection.
     * @param c the JDBC connection to use
     */
    public GetChart(Connection c) {
        super(c);
    }

    /**
     * Returns all Airports with available charts
     * @return a List of Airports
     * @throws DAOException if a JDBC error occurs
     */
    public List getAirports() throws DAOException {
        try {
            prepareStatementWithoutLimits("SELECT DISTINCT (IATA) FROM common.CHARTS");
            
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
    
    /**
     * Returns all Charts for a particular Airport code.
     * @param iataCode the Airport's IATA code
     * @return a List of Chart objects
     * @throws DAOException if a JDBC error occurs
     * @see GetChart#getCharts(Airport)
     */
    public List getCharts(String iataCode) throws DAOException {
        try {
            // Prepare the statement
            prepareStatement("SELECT ID, NAME, IATA, TYPE, SIZE FROM common.CHARTS WHERE (IATA=?) ORDER BY NAME");
            _ps.setString(1, iataCode);
            
            // Execute the query
            return execute();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Returns all Charts for a particular Airport.
     * @param a the Airport object
     * @return a List of Chart objects
     * @throws DAOException if a JDBC error occurs
     * @see GetChart#getCharts(String)
     */
    public List getCharts(Airport a) throws DAOException {
        return getCharts(a.getIATA());
    }
    
    /**
     * Returns all Charts for a particular Event.
     * @param eventID the event Database ID
     * @return a List of Chart objects
     * @throws DAOException if a JDBC error occurs
     */
    public List getChartsByEvent(int eventID) throws DAOException {
       try {
          prepareStatement("SELECT C.ID, C.NAME, C.IATA, C.TYPE, C.SIZE FROM common.CHARTS C, "
          		+ "common.EVENT_CHARTS EC WHERE (EC.ID=?) AND (C.ID=EC.CHART) ORDER BY C.NAME");
          _ps.setInt(1, eventID);
          
          // Execute the query
          return execute();
       } catch (SQLException se) {
          throw new DAOException(se);
       }
    }
    
    /**
     * Returns a Chart based on its Database ID. 
     * @param id the database id
     * @return the Chart
     * @throws DAOException if a JDBC error occurs
     */
    public Chart get(int id) throws DAOException {
        try {
            // Prepare the statement
            prepareStatement("SELECT ID, NAME, IATA, TYPE, SIZE FROM common.CHARTS WHERE (ID=?)");
            _ps.setInt(1, id);
            
            // Execute the query
            _ps.setMaxRows(1);
            List results = execute();
            return (results.isEmpty()) ? null : (Chart) results.get(0);
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Helper method to load chart metadata.
     */
    private List execute() throws SQLException {
       List results = new ArrayList();
       
       // Execute the query
       ResultSet rs = _ps.executeQuery();
       while (rs.next()) {
          Chart c = new Chart(rs.getString(2), SystemData.getAirport(rs.getString(3)));
          c.setID(rs.getInt(1));
          c.setType(rs.getInt(4));
          c.setSize(rs.getInt(5));
          
          // Add to results
          results.add(c);
       }
       
       // Clean up and return
       rs.close();
       _ps.close();
       return results;
    }
}