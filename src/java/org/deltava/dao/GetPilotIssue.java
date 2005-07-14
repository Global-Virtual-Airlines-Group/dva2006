// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.FlightReport;

/**
 * A DAO to get Pilot object(s) from the database for Issue Tracking operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetPilotIssue extends PilotReadDAO {

    /**
     * Creates the DAO from a JDBC connection.
     * @param c the JDBC connection
     */
    public GetPilotIssue(Connection c) {
        super(c);
    }
    
    /**
     * Returns all Pilots who have a particular security role.
     * @param roleName the role name
     * @return a List of Pilots
     * @throws DAOException if a JDBC error occurs
     */
    public List getPilotsByRole(String roleName) throws DAOException {
    	try {
            prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
                	+ "MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON (P.ID=F.PILOT_ID) LEFT JOIN ROLES R ON (P.ID=R.ID) "
                	+ "WHERE (R.ROLE=?) AND (F.STATUS=?) GROUP BY P.ID");
    		_ps.setString(1, roleName);
    		_ps.setInt(2, FlightReport.OK);
    		return execute();
    	} catch (SQLException se) {
            throw new DAOException(se);
        }
    }
}