// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Staff;

/**
 * A Data Access Object to return Staff Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetStaff extends DAO {

    /**
     * Initializes the Data Access Object. 
     * @param c the JDBC connection to use
     */
    public GetStaff(Connection c) {
        super(c);
    }

    /**
     * Returns the staff profile corresponding to a particular database ID.
     * @param id the pilot ID
     * @return the Staff profile
     * @throws DAOException if a JDBC error occurs
     */
    public Staff get(int id) throws DAOException {
        try {
            prepareStatement("SELECT P.FIRSTNAME, P.LASTNAME, P.EMAIL, S.* FROM STAFF S, PILOTS P WHERE " +
                    "(S.ID=P.ID) AND (S.ID=?)");
            _ps.setInt(1, id);
			_ps.setMaxRows(1);
            
            // Execute the query and get the result; if none return null
            ResultSet rs = _ps.executeQuery();
            if (!rs.next()) {
            	rs.close();
            	_ps.close();
            	return null;
            }
            
            // Create the staff object
            Staff s = new Staff(rs.getString(1), rs.getString(2));
            s.setEMail(rs.getString(3));
            s.setID(rs.getInt(4));
            s.setTitle(rs.getString(5));
            s.setSortOrder(rs.getInt(6));
            s.setBody(rs.getString(7));
            s.setArea(rs.getString(8));
            
            // Clean up and return
            rs.close();
            _ps.close();
            return s;
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Returns all Staff Profile objects from the database.
     * @return a List of Staff Profiles
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<Staff> getStaff() throws DAOException {
        try {
            prepareStatement("SELECT P.FIRSTNAME, P.LASTNAME, P.EMAIL, S.* FROM STAFF S, PILOTS P WHERE (S.ID=P.ID)");
            
            // Execute the query
            Collection<Staff> results = new ArrayList<Staff>();
            ResultSet rs = _ps.executeQuery();
            while (rs.next()) {
                Staff s = new Staff(rs.getString(1), rs.getString(2));
                s.setEMail(rs.getString(3));
                s.setID(rs.getInt(4));
                s.setTitle(rs.getString(5));
                s.setSortOrder(rs.getInt(6));
                s.setBody(rs.getString(7));
                s.setArea(rs.getString(8));

                // Add to the results
                results.add(s);
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