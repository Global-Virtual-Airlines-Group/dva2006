// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.deltava.beans.FlightReport;

/**
 * A Data Access Object to get Flight Reports for Pilot recognition. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetFlightReportRecognition extends GetFlightReports {

   /**
    * Initializes the Data Access Object.
    * @param c the JDBC Connection to use
    */
   public GetFlightReportRecognition(Connection c) {
      super(c);
   }

	/**
	 * Returns Flight Reports with the smoothest touchdown speed.
	 * @param days the number of days in the past to search
	 * @return a List of ACARSFlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getGreasedLandings(int days) throws DAOException {
		
		// Build the SQL statement
		StringBuffer sqlBuf = new StringBuffer("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, APR.* FROM PILOTS P LEFT JOIN "
				+ "PIREPS PR ON (PR.PILOT_ID=P.ID) LEFT JOIN ACARS_PIREPS APR ON (PR.ID=APR.ID) WHERE (PR.STATUS=?) "
				+ "AND (APR.LANDING_VSPEED < 0)");
		
		// Append number of days
		if (days > 0)
			sqlBuf.append(" AND (DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		
		sqlBuf.append(" ORDER BY APR.LANDING_VSPEED DESC, PR.DATE DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, FlightReport.OK);
			if (days > 0)
				_ps.setInt(2, days);

			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves ACARS Flight Reports logged by staff members.
	 * @param days the number of days in the past to search
	 * @return a List of ACARSFlightReport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getStaffReports(int days) throws DAOException {
	   
	   // Build the SQL statement
	   StringBuffer sqlBuf = new StringBuffer("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, APR.* FROM PILOTS P, "
	         + "STAFF S LEFT JOIN PIREPS PR ON (PR.PILOT_ID=P.ID) LEFT JOIN ACARS_PIREPS APR ON (PR.ID=APR.ID) "
	         + "WHERE (P.ID=S.ID) AND (PR.STATUS=?) AND (APR.LANDING_VSPEED < 0)");
	   
		// Append number of days
		if (days > 0)
			sqlBuf.append(" AND (DATE > DATE_SUB(NOW(), INTERVAL ? DAY))");
		
		sqlBuf.append(" ORDER BY APR.LANDING_VSPEED DESC, PR.DATE DESC");
		
	   try {
	      prepareStatement(sqlBuf.toString());
	      _ps.setInt(1, FlightReport.OK);
	      if (days > 0)
				_ps.setInt(2, days);
	      
	      return execute();
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
}