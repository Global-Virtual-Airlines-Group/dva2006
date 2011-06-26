// Copyright 2005, 2007, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;

/**
 * A Data Access Object to merge a Pilot's data into another.
 * @author Luke
 * @version 4.0
 * @since 1.0
 */

public class SetPilotMerge extends PilotWriteDAO {
   
   private static final Logger log = Logger.getLogger(SetPilotMerge.class);

   /**
    * Initializes the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetPilotMerge(Connection c) {
      super(c);
   }

   /**
    * Merges Flight Reports. 
    * @param oldUser the old Pilot bean
    * @param newUser the new Pilot bean
    * @return the number of FlightReports updated
    * @throws DAOException if a JDBC error occurs
    */
   public int mergeFlights(Pilot oldUser, Pilot newUser) throws DAOException {
	   invalidate(oldUser.getID());
	   invalidate(newUser.getID());
	   try {
		   prepareStatementWithoutLimits("UPDATE PIREPS SET PILOT_ID=? WHERE (PILOT_ID=?)");
		   _ps.setInt(1, newUser.getID());
		   _ps.setInt(2, oldUser.getID());
		   int rowsUpdated = executeUpdate(0);
		   if (rowsUpdated > 0)
			   log.info("Moved " + rowsUpdated + " Flight Reports from " + oldUser.getName() + " to " + newUser.getName());
		   
		   return rowsUpdated;
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }
   
   /**
    * Merges Examinations. 
    * @param oldUser the old Pilot bean
    * @param newUser the new Pilot bean
    * @return the number of Examinations updated
    * @throws DAOException if a JDBC error occurs
    */
   public int mergeExams(Pilot oldUser, Pilot newUser) throws DAOException {
	   invalidate(oldUser.getID());
	   invalidate(newUser.getID());
	   try {
		   prepareStatementWithoutLimits("UPDATE exams.EXAMS SET PILOT_ID=? WHERE (PILOT_ID=?)");
		   _ps.setInt(1, newUser.getID());
	       _ps.setInt(2, oldUser.getID());
	       int rowsUpdated = executeUpdate(0);
	       if (rowsUpdated > 0)
	    	   log.info("Moved " + rowsUpdated + " Examinations from " + oldUser.getName() + " to " + newUser.getName());
	         
	       return rowsUpdated;
	   } catch (SQLException se) {
	       throw new DAOException(se);
	   }
   }
   
   /**
    * Merges Check Rides. 
    * @param oldUser the old Pilot bean
    * @param newUser the new Pilot bean
    * @return the number of Check Rides updated
    * @throws DAOException if a JDBC error occurs
    */
   public int mergeCheckRides(Pilot oldUser, Pilot newUser) throws DAOException {
	   invalidate(oldUser.getID());
	   invalidate(newUser.getID());
	   try {
	       prepareStatementWithoutLimits("UPDATE exams.CHECKRIDES SET PILOT_ID=? WHERE (PILOT_ID=?)");
	       _ps.setInt(1, newUser.getID());
	       _ps.setInt(2, oldUser.getID());
	       int rowsUpdated = executeUpdate(0);
	       if (rowsUpdated > 0)
	    	   log.info("Moved " + rowsUpdated + " Check Rides from " + oldUser.getName() + " to " + newUser.getName());
	       
	       return rowsUpdated;
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }
   
   /**
    * Merges Flight Academy courses.
    * @param oldUser the old Pilot bean
    * @param newUser the new Pilot bean
    * @return the number of Courses updated
    * @throws DAOException if a JDBC error occurs
    */
   public int mergeCourses(Pilot oldUser, Pilot newUser) throws DAOException {
	   invalidate(oldUser.getID());
	   invalidate(newUser.getID());
	   try {
		   prepareStatementWithoutLimits("UPDATE exams.COURSES SET PILOT_ID=? WHERE (PILOT_ID=?)");
	       _ps.setInt(1, newUser.getID());
	       _ps.setInt(2, oldUser.getID());
	       int rowsUpdated = executeUpdate(0);
	       if (rowsUpdated > 0)
	    	   log.info("Moved " + rowsUpdated + " Check Rides from " + oldUser.getName() + " to " + newUser.getName());
		   
	       return rowsUpdated;
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }
}