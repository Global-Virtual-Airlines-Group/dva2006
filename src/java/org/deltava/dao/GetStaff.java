// Copyright 2005, 2006, 2007, 2008, 2011, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Rank;
import org.deltava.beans.Staff;

/**
 * A Data Access Object to return Staff Profiles.
 * @author Luke
 * @version 8.3
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
     * @return the Staff profile, or null if not found
     * @throws DAOException if a JDBC error occurs
     */
    public Staff get(int id) throws DAOException {
        try {
            prepareStatementWithoutLimits("SELECT P.FIRSTNAME, P.LASTNAME, P.EMAIL, P.EQTYPE, P.RANKING, S.* FROM STAFF S, PILOTS P WHERE (S.ID=P.ID) AND (S.ID=?) LIMIT 1");
            _ps.setInt(1, id);
            Staff s = null;
            try (ResultSet rs = _ps.executeQuery()) {
            	if (rs.next()) {
            		s = new Staff(rs.getString(1), rs.getString(2));
            		s.setEmail(rs.getString(3));
            		s.setEquipmentType(rs.getString(4));
            		s.setRank(Rank.values()[rs.getInt(5)]);
            		s.setID(rs.getInt(6));
            		s.setTitle(rs.getString(7));
            		s.setSortOrder(rs.getInt(8));
            		s.setBody(rs.getString(9));
            		s.setArea(rs.getString(10));
            	}
            }
            
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
            prepareStatementWithoutLimits("SELECT P.FIRSTNAME, P.LASTNAME, P.EMAIL, P.EQTYPE, P.RANKING, S.* FROM STAFF S, PILOTS P WHERE (S.ID=P.ID)");
            Collection<Staff> results = new ArrayList<Staff>();
            try (ResultSet rs = _ps.executeQuery()) {
            	while (rs.next()) {
            		Staff s = new Staff(rs.getString(1), rs.getString(2));
            		s.setEmail(rs.getString(3));
            		s.setEquipmentType(rs.getString(4));
            		s.setRank(Rank.values()[rs.getInt(5)]);
            		s.setID(rs.getInt(6));
            		s.setTitle(rs.getString(7));
            		s.setSortOrder(rs.getInt(8));
            		s.setBody(rs.getString(9));
            		s.setArea(rs.getString(10));
            		results.add(s);
            	}
            }
            
            _ps.close();
            return results;
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
}