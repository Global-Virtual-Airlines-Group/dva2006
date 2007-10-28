// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;

/**
 * A Data Access Object to merge a Pilot's data into another.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetPilotMerge extends DAO {
   
   private static final Logger log = Logger.getLogger(SetPilotMerge.class);

   /**
    * Initializes the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetPilotMerge(Connection c) {
      super(c);
   }

   /**
    * Merges Flight Reports, Examinations and Checkrides, and deactivates the old Pilot. 
    * @param oldUser the old Pilot bean
    * @param newUser the new Pilot bean
    * @throws DAOException if a JDBC error occurs
    */
   public void merge(Pilot oldUser, Pilot newUser) throws DAOException {
      try {
         startTransaction();
         
         // Merge PIREPs
         prepareStatementWithoutLimits("UPDATE PIREPS SET PILOT_ID=? WHERE (PILOT_ID=?)");
         _ps.setInt(1, newUser.getID());
         _ps.setInt(2, oldUser.getID());
         int rowsUpdated = executeUpdate(0);
         if (rowsUpdated > 0)
            log.info("Moved " + rowsUpdated + " Flight Reports from " + oldUser.getName() + " to " + newUser.getName());
         
         // Merge Examinations
         prepareStatementWithoutLimits("UPDATE exams.EXAMS SET PILOT_ID=? WHERE (PILOT_ID=?)");
         _ps.setInt(1, newUser.getID());
         _ps.setInt(2, oldUser.getID());
         rowsUpdated = executeUpdate(0);
         if (rowsUpdated > 0)
            log.info("Moved " + rowsUpdated + " Examinations from " + oldUser.getName() + " to " + newUser.getName());
         
         // Merge Check Rides
         prepareStatementWithoutLimits("UPDATE exams.CHECKRIDES SET PILOT_ID=? WHERE (PILOT_ID=?)");
         _ps.setInt(1, newUser.getID());
         _ps.setInt(2, oldUser.getID());
         rowsUpdated = executeUpdate(0);
         if (rowsUpdated > 0)
            log.info("Moved " + rowsUpdated + " Check Rides from " + oldUser.getName() + " to " + newUser.getName());
         
         // Update the pilot
         prepareStatement("UPDATE PILOTS SET STATUS=? WHERE (ID=?)");
         _ps.setInt(1, Pilot.RETIRED);
         _ps.setInt(2, oldUser.getID());
         executeUpdate(1);
         
         // Commit the transaction
         commitTransaction();
      } catch (SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }
}