// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.List;

import org.deltava.beans.*;

/**
 * A Data Acccess Object to read Pilots that have achieved certain accomplishments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetPilotRecognition extends PilotReadDAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetPilotRecognition(Connection c) {
		super(c);
	}

    /**
     * Returns members of the &quot;Century Club&quot;, with over 100 Flight Legs.
     * @return the Pilot, or null if not found
     * @throws DAOException if a JDBC error occurs
     */
    public List getCenturyClub() throws DAOException {
    	
        // Init the prepared statement
        try {
            prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), " +
                    "MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) GROUP BY P.ID " +
                    "HAVING (LEGS >= 100) ORDER BY LEGS DESC");
            _ps.setInt(1, FlightReport.OK);
            return execute();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Returns Pilots eligible for promotion to Captain.
     * @return a List of Pilots
     * @throws DAOException if a JDBC error occurs
     */
    public List getPromotionQueue() throws DAOException {
       try {
          prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
                + "MAX(F.DATE), (SELECT COUNT(DISTINCT F.ID) FROM PIREPS F, PROMO_EQ PEQ WHERE (F.PILOT_ID=P.ID) AND "
                + "(F.ID=PEQ.ID) AND (PEQ.EQTYPE=P.EQTYPE) AND (F.STATUS=?)) AS CLEGS, EQ.C_LEGS FROM PILOTS P "
                + "LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN EQTYPES EQ ON "
                + "(P.EQTYPE=EQ.EQTYPE) LEFT JOIN EXAMS EX ON ((EX.PILOT_ID=P.ID) AND (EX.NAME=EQ.EXAM_CAPT)) "
                + "WHERE (P.STATUS=?) AND (P.RANK=?) AND (EX.PASS=?) GROUP BY P.ID HAVING (CLEGS >= EQ.C_LEGS) "
                + "ORDER BY CLEGS DESC");
          _ps.setInt(1, FlightReport.OK);
          _ps.setInt(2, FlightReport.OK);
          _ps.setInt(3, Pilot.ACTIVE);
          _ps.setString(4, Ranks.RANK_FO);
          _ps.setBoolean(5, true);
          return execute();
       } catch (SQLException se) {
          throw new DAOException(se);
       }
    }
}