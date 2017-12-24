// Copyright 2005, 2009, 2010, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;

/**
 * A Data Access Object to create and update Flight Assignments.
 * @author Luke
 * @version 8.1
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
         prepareStatement(sqlBuf.toString());
         _ps.setInt(1, a.getStatus().ordinal());
         _ps.setInt(2, a.getEventID());
         _ps.setInt(3, a.getPilotID());
         _ps.setTimestamp(4, createTimestamp(a.getAssignDate()));
         _ps.setString(5, a.getEquipmentType());
         _ps.setBoolean(6, a.isRepeating());
         _ps.setBoolean(7, a.isRandom());
         _ps.setBoolean(8, a.isPurgeable());
         executeUpdate(1);

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

      try {
         prepareStatement(sqlBuf.toString());
         _ps.setInt(1, AssignmentStatus.RESERVED.ordinal());
         _ps.setInt(2, pilotID);
         _ps.setInt(3, a.getID());
         executeUpdate(1);
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
      prepareStatement(sqlBuf.toString());
      _ps.setInt(1, assignID);

      // Write the legs
      for (AssignmentLeg leg : legs) {
         _ps.setString(2, leg.getAirline().getCode());
         _ps.setInt(3, leg.getFlightNumber());
         _ps.setInt(4, leg.getLeg());
         _ps.setString(5, leg.getAirportD().getIATA());
         _ps.setString(6, leg.getAirportA().getIATA());
         _ps.addBatch();
      }

      executeBatchUpdate(1, legs.size());
   }

   /**
    * Marks an Assignment as Complete.
    * @param ai the AssignmentInfo object
    * @throws DAOException if a JDBC error occurs
    */
   public void complete(AssignmentInfo ai) throws DAOException {
      try {
         prepareStatement("UPDATE ASSIGNMENTS SET STATUS=?, COMPLETED_ON=NOW() WHERE (ID=?)");
         _ps.setInt(1, AssignmentStatus.COMPLETE.ordinal());
         _ps.setInt(2, ai.getID());
         executeUpdate(1);
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
         prepareStatement("UPDATE ASSIGNMENTS SET PILOT_ID=0, STATUS=?, ASSIGNED_ON=NULL WHERE (ID=?)");
         _ps.setInt(1, AssignmentStatus.AVAILABLE.ordinal());
         _ps.setInt(2, a.getID());
         executeUpdate(1);
         
         // Delete the draft Flight Reports
         prepareStatement("DELETE FROM PIREPS WHERE (ASSIGN_ID=?) AND (STATUS=?)");
         _ps.setInt(1, a.getID());
         _ps.setInt(2, FlightReport.DRAFT);
         executeUpdate(0);

         // Clear the Flight Reports
         prepareStatement("UPDATE PIREPS SET ASSIGN_ID=0 WHERE (ASSIGN_ID=?)");
         _ps.setInt(1, a.getID());
         executeUpdate(0);
         
         // Commit the transaction
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

         // Delete legs
         prepareStatement("DELETE FROM ASSIGNLEGS WHERE (ID=?)");
         _ps.setInt(1, a.getID());
         executeUpdate(0);

         // Delete assignment
         prepareStatement("DELETE FROM ASSIGNMENTS WHERE (ID=?)");
         _ps.setInt(1, a.getID());
         executeUpdate(1);
         
         // Clear the Flown Flight Reports
         prepareStatement("UPDATE PIREPS SET ASSIGN_ID=0 WHERE (ASSIGN_ID=?) AND (STATUS IN (?, ?, ?))");
         _ps.setInt(1, a.getID());
         _ps.setInt(2, FlightReport.OK);
         _ps.setInt(3, FlightReport.SUBMITTED);
         _ps.setInt(4, FlightReport.HOLD);
         executeUpdate(0);
         
         // Delete the incomplete/rejected Flight Reports
         prepareStatement("DELETE FROM PIREPS WHERE (ASSIGN_ID=?) AND ((STATUS=?) OR (STATUS=?))");
         _ps.setInt(1, a.getID());
         _ps.setInt(2, FlightReport.DRAFT);
         _ps.setInt(3, FlightReport.REJECTED);
         executeUpdate(0);
         commitTransaction();
      } catch (SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }
}