// Copyright 2005, 2009, 2010, 2017, 2018, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;

/**
 * A Data Access Object to create and update Flight Assignments.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class SetAssignment extends DAO {

   /**
    * Initialize the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetAssignment(Connection c) {
      super(c);
   }

   /**
    * Writes a new Flight Assignment to the database. Any FlightReport beans within the assignment will have the
    * Assignment Database ID updated.
    * @param a the Assingment object
    * @param db the Database to write to
    * @throws DAOException if a JDBC error occurs
    */
   public void write(AssignmentInfo a, String db) throws DAOException {

      // Build the SQL statement
      StringBuilder sqlBuf = new StringBuilder("INSERT INTO ");
      sqlBuf.append(formatDBName(db));
      sqlBuf.append(".ASSIGNMENTS (STATUS, EVENT_ID, PILOT_ID, ASSIGNED_ON, EQTYPE, REPEATS, RANDOM, PURGEABLE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

      try {
         startTransaction();
         try (PreparedStatement ps = prepare(sqlBuf.toString())) {
        	 ps.setInt(1, a.getStatus().ordinal());
        	 ps.setInt(2, a.getEventID());
        	 ps.setInt(3, a.getPilotID());
        	 ps.setTimestamp(4, createTimestamp(a.getAssignDate()));
        	 ps.setString(5, a.getEquipmentType());
        	 ps.setBoolean(6, a.isRepeating());
        	 ps.setBoolean(7, a.isRandom());
        	 ps.setBoolean(8, a.isPurgeable());
        	 executeUpdate(ps, 1);
         }

         // Update the ID
         a.setID(getNewID());

         // Write the legs
         writeLegs(a.getID(), a.getAssignments(), db);

         // Update the Flight Reports with the new database ID
         a.getFlights().forEach(fr -> fr.setDatabaseID(DatabaseID.ASSIGN, a.getID()));
         commitTransaction();
      } catch (SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }

   /**
    * Assigns a Flight Assignment to a particular Pilot.
    * @param a the Assignment, with status and assignDate properties set
    * @param pilotID the Pilot's Database ID
    * @param db the Database to write to
    * @throws DAOException if a JDBC error occurs
    */
   public void assign(AssignmentInfo a, int pilotID, String db) throws DAOException {

      // Build the SQL statement
      StringBuilder sqlBuf = new StringBuilder("UPDATE ");
      sqlBuf.append(formatDBName(db));
      sqlBuf.append(".ASSIGNMENTS SET ASSIGNED_ON=NOW(), STATUS=?, PILOT_ID=? WHERE (ID=?)");

      try (PreparedStatement ps = prepare(sqlBuf.toString())) {
         ps.setInt(1, AssignmentStatus.RESERVED.ordinal());
         ps.setInt(2, pilotID);
         ps.setInt(3, a.getID());
         executeUpdate(ps, 1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /*
    * Private method to write the individual legs of a Flight Assignment to the database.
    */
   private void writeLegs(int assignID, Collection<AssignmentLeg> legs, String db) throws SQLException {

      // Prepare the SQL statement
      StringBuilder sqlBuf = new StringBuilder("REPLACE INTO ");
      sqlBuf.append(formatDBName(db));
      sqlBuf.append(".ASSIGNLEGS (ID, AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A) VALUES (?, ?, ?, ?, ?, ?)");
      try (PreparedStatement ps = prepare(sqlBuf.toString())) {
    	  ps.setInt(1, assignID);
    	  for (AssignmentLeg leg : legs) {
    		  ps.setString(2, leg.getAirline().getCode());
    		  ps.setInt(3, leg.getFlightNumber());
    		  ps.setInt(4, leg.getLeg());
    		  ps.setString(5, leg.getAirportD().getIATA());
    		  ps.setString(6, leg.getAirportA().getIATA());
    		  ps.addBatch();
    	  }

    	  executeUpdate(ps, 1, legs.size());
      }
   }

   /**
    * Marks an Assignment as Complete.
    * @param ai the AssignmentInfo object
    * @throws DAOException if a JDBC error occurs
    */
   public void complete(AssignmentInfo ai) throws DAOException {
	   try (PreparedStatement ps = prepare("UPDATE ASSIGNMENTS SET STATUS=?, COMPLETED_ON=NOW() WHERE (ID=?)")) {
         ps.setInt(1, AssignmentStatus.COMPLETE.ordinal());
         ps.setInt(2, ai.getID());
         executeUpdate(ps, 1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Releases a Flight Assignment.
    * @param a the Flight Assignment bean
    * @throws DAOException if a JDBC error occurs
    */
   public void reset(AssignmentInfo a) throws DAOException {
      try {
         startTransaction();
         
         // Release the assignment
         try (PreparedStatement ps = prepare("UPDATE ASSIGNMENTS SET PILOT_ID=0, STATUS=?, ASSIGNED_ON=NULL WHERE (ID=?)")) {
        	 ps.setInt(1, AssignmentStatus.AVAILABLE.ordinal());
        	 ps.setInt(2, a.getID());
        	 executeUpdate(ps, 1);
         }
         
         // Delete the draft Flight Reports
         try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM PIREPS WHERE (ASSIGN_ID=?) AND (STATUS=?)")) {
        	 ps.setInt(1, a.getID());
        	 ps.setInt(2, FlightStatus.DRAFT.ordinal());
        	 executeUpdate(ps, 0);
         }

         // Clear the Flight Reports
         try (PreparedStatement ps = prepare("UPDATE PIREPS SET ASSIGN_ID=0 WHERE (ASSIGN_ID=?)")) {
        	 ps.setInt(1, a.getID());
        	 executeUpdate(ps, 0);
         }
         
         commitTransaction();
      } catch (SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }

   /**
    * Deletes a Flight Assignment from the database.
    * @param a the Assignment
    * @throws DAOException if a JDBC error occurs
    */
   public void delete(AssignmentInfo a) throws DAOException {
      try {
         startTransaction();

         // Clear the flown Flight Reports
         try (PreparedStatement ps = prepareWithoutLimits("UPDATE PIREPS SET ASSIGN_ID=0 WHERE (ASSIGN_ID=?) AND (STATUS<>?)")) {
        	 ps.setInt(1, a.getID());
        	 ps.setInt(2, FlightStatus.DRAFT.ordinal());
        	 executeUpdate(ps, 0);
         }
         
         // Delete the incomplete/rejected Flight Reports
         try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM PIREPS WHERE (ASSIGN_ID=?) AND (STATUS=?)")) {
        	 ps.setInt(1, a.getID());
        	 ps.setInt(2, FlightStatus.DRAFT.ordinal());
        	 executeUpdate(ps, 0);
         }

         // Delete legs
         try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM ASSIGNLEGS WHERE (ID=?)")) {
        	 ps.setInt(1, a.getID());
        	 executeUpdate(ps, 0);
         }

         // Delete assignment
         try (PreparedStatement ps = prepare("DELETE FROM ASSIGNMENTS WHERE (ID=?)")) {
        	 ps.setInt(1, a.getID());
        	 executeUpdate(ps, 1);
         }
         
         commitTransaction();
      } catch (SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }
  
   /**
    * Writes a Charter flight request to the database.
    * @param req the CharterRequest
    * @throws DAOException if a JDBC error occurs
    */
   public void write(CharterRequest req) throws DAOException {
	   try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO CHARTER_REQUESTS (ID, AUTHOR_ID, CREATED, AIRPORT_D, AIRPORT_A, AIRLINE, EQTYPE, DISPOSAL_ID, DISPOSED, STATUS, REMARKS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
		   ps.setInt(1, req.getID());
		   ps.setInt(2, req.getAuthorID());
		   ps.setTimestamp(3, createTimestamp(req.getCreatedOn()));
		   ps.setString(4, req.getAirportD().getIATA());
		   ps.setString(5, req.getAirportA().getIATA());
		   ps.setString(6, req.getAirline().getCode());
		   ps.setString(7, req.getEquipmentType());
		   ps.setInt(8, req.getDisposalID());
		   ps.setTimestamp(9, createTimestamp(req.getDisposedOn()));
		   ps.setInt(10, req.getStatus().ordinal());
		   ps.setString(11, req.getComments());
		   executeUpdate(ps, 1);
		   if (req.getID() == 0)
			   req.setID(getNewID());
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }
  
   /**
    * Deletes a Charter flight request from the database.
    * @param req the CharterRequest
    * @throws DAOException if a JDBC error occurs
    */
   public void delete(CharterRequest req) throws DAOException {
	   try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM CHARTER_REQUESTS WHERE (ID=?) LIMIT 1")) {
		   ps.setInt(1, req.getID());
		   executeUpdate(ps, 0);
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }
}