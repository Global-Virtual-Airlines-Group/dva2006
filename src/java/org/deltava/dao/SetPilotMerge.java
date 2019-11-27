// Copyright 2005, 2007, 2010, 2011, 2012, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;

import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to merge a Pilot's data into another.
 * @author Luke
 * @version 9.0
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
	   try (PreparedStatement ps = prepareWithoutLimits("UPDATE PIREPS SET PILOT_ID=? WHERE (PILOT_ID=?)")) {
		   ps.setInt(1, newUser.getID());
		   ps.setInt(2, oldUser.getID());
		   int rowsUpdated = executeUpdate(ps, 0);
		   if (rowsUpdated > 0)
			   log.info("Moved " + rowsUpdated + " Flight Reports from " + oldUser.getName() + " to " + newUser.getName());
		   
		   return rowsUpdated;
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   } finally {
		   CacheManager.invalidate("Pilots", Integer.valueOf(oldUser.getID()));
		   CacheManager.invalidate("Pilots", Integer.valueOf(newUser.getID()));
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
	   try (PreparedStatement ps = prepareWithoutLimits("UPDATE exams.EXAMS SET PILOT_ID=? WHERE (PILOT_ID=?)")) {
		   ps.setInt(1, newUser.getID());
	       ps.setInt(2, oldUser.getID());
	       int rowsUpdated = executeUpdate(ps, 0);
	       if (rowsUpdated > 0)
	    	   log.info("Moved " + rowsUpdated + " Examinations from " + oldUser.getName() + " to " + newUser.getName());
	         
	       return rowsUpdated;
	   } catch (SQLException se) {
	       throw new DAOException(se);
	   } finally {
		   CacheManager.invalidate("Pilots", Integer.valueOf(oldUser.getID()));
		   CacheManager.invalidate("Pilots", Integer.valueOf(newUser.getID()));
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
	   try (PreparedStatement ps = prepareWithoutLimits("UPDATE exams.CHECKRIDES SET PILOT_ID=? WHERE (PILOT_ID=?)")) {
	       ps.setInt(1, newUser.getID());
	       ps.setInt(2, oldUser.getID());
	       int rowsUpdated = executeUpdate(ps, 0);
	       if (rowsUpdated > 0)
	    	   log.info("Moved " + rowsUpdated + " Check Rides from " + oldUser.getName() + " to " + newUser.getName());
	       
	       return rowsUpdated;
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   } finally {
		   CacheManager.invalidate("Pilots", Integer.valueOf(oldUser.getID()));
		   CacheManager.invalidate("Pilots", Integer.valueOf(newUser.getID()));
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
	   try (PreparedStatement ps = prepareWithoutLimits("UPDATE exams.COURSES SET PILOT_ID=? WHERE (PILOT_ID=?)")) {
	       ps.setInt(1, newUser.getID());
	       ps.setInt(2, oldUser.getID());
	       int rowsUpdated = executeUpdate(ps, 0);
	       if (rowsUpdated > 0)
	    	   log.info("Moved " + rowsUpdated + " Check Rides from " + oldUser.getName() + " to " + newUser.getName());
		   
	       return rowsUpdated;
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   } finally {
		   CacheManager.invalidate("Pilots", Integer.valueOf(oldUser.getID()));
		   CacheManager.invalidate("Pilots", Integer.valueOf(newUser.getID()));
	   }
   }
}