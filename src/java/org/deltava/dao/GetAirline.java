package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.Airline;

/**
 * A Data Access Object to load Airline codes and names.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class GetAirline extends DAO {

    /**
     * Creates the DAO with a JDBC connection.
     * @param c the JDBC connection to use
     */
    public GetAirline(Connection c) {
        super(c);
    }
    
    // Executes a query returning multiple airline objects
    private Map execute(String sql) throws DAOException {
        try {
            prepareStatementWithoutLimits(sql);
            Map results = new HashMap();
            
            // Execute the query
            ResultSet rs = _ps.executeQuery();
            while (rs.next()) {
                Airline a = new Airline(rs.getString(1), rs.getString(2));
                a.setActive(rs.getBoolean(3));
                results.put(a.getCode(), a);
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
     * Returns all Airlines from the database.
     * @return a Map of Airline objects, with the code as the key
     * @throws DAOException if a JDBC error occurs
     */
    public Map getAll() throws DAOException {
        return execute("SELECT * FROM common.AIRLINES ORDER BY CODE");
    }
    
    /**
     * Returns all active Airlines from the database.
     * @return a Map of Airline objects where isActive() == TRUE with the code as the key
     * @throws DAOException if a JDBC error occurs
     */
    public Map getActive() throws DAOException {
        return execute("SELECT * FROM common.AIRLINES WHERE (ACTIVE=TRUE) ORDER BY CODE");
    }
   
    /**
     * Returns an Airline object.
     * @param code the Airline code to get
     * @return the Airline, or null if the code was not found
     * @throws DAOException if a JDBC error occurs
     * @throws NullPointerException if code is null
     */
    public Airline get(String code) throws DAOException {
        try {
            prepareStatementWithoutLimits("SELECT * FROM common.AIRLINES WHERE (CODE=?)");
            _ps.setString(1, code);
            
            // Execute the query, if nothing matches return null
            ResultSet rs = _ps.executeQuery();
            if (!rs.next())
                return null;
            
            // Create the airline object
            Airline a = new Airline(rs.getString(1), rs.getString(2));
            a.setActive(rs.getBoolean(3));

            // Clean up and return
            rs.close();
            _ps.close();
            return a;
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
}