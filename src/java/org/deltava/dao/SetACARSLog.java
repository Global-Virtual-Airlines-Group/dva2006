// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

/**
 * A Data Access Object to remove ACARS log entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetACARSLog extends DAO {

   /**
    * Initializes the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetACARSLog(Connection c) {
      super(c);
   }

   /**
    * Deletes an ACARS Connection log entry.
    * @param id the connection ID
    * @throws DAOException if a JDBC error occurs
    */
   public void deleteConnection(long id) throws DAOException {
      try {
         prepareStatement("DELETE FROM acars.CONS WHERE (ID=?)");
         _ps.setLong(1, id);
         executeUpdate(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Deletes an ACARS Flight Information log entry, and associated position reports.
    * @param flightID the flight ID
    * @throws DAOException if a JDBC error occurs
    */
   public void deleteInfo(int flightID) throws DAOException {
      try {
         prepareStatement("DELETE FROM acars.FLIGHTS WHERE (ID=?) AND (ARCHIVED=?)");
         _ps.setInt(1, flightID);
         _ps.setBoolean(2, false);
         executeUpdate(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Deletes ACARS text messages older than a specified number of hours.
    * @param hours the number of hours
    * @return the number of messages purged
    * @throws DAOException if a JDBC error occurs
    */
   public int purgeMessages(int hours) throws DAOException {
	   try {
		   prepareStatement("DELETE FROM acars.MESSAGES WHERE (DATE < DATE_SUB(NOW(), INTERVAL ? HOUR))");
		   _ps.setInt(1, hours);
		   return executeUpdate(0);
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }
   
   /**
    * Deletes unfiled ACARS flight information older than a specified number of hours.
    * @param hours the number of hours
    * @return the number of flights purged
    * @throws DAOException if a JDBC error occurs
    */
   public int purgeFlights(int hours) throws DAOException {
	   try {
		   prepareStatement("DELETE acars.FLIGHTS.* FROM acars.FLIGHTS, acars.CONS WHERE "
				   + "(acars.CONS.ID=acars.FLIGHTS.CON_ID) AND (acars.FLIGHTS.PIREP=?) AND (acars.FLIGHTS.CREATED "
				   + "< DATE_SUB(NOW(), INTERVAL ? HOUR))");
		   _ps.setBoolean(1, false);
		   _ps.setInt(2, hours);
		   return executeUpdate(0);
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }
   
   /**
    * Moves ACARS position data from the live table to the archive.
    * @param flightID the ACARS Flight ID
    * @throws DAOException if a JDBC error occurs
    */
   public void archivePositions(int flightID) throws DAOException {
     try {
        startTransaction();
        
        // Copy the data to the archive
        prepareStatementWithoutLimits("INSERT INTO acars.POSITION_ARCHIVE SELECT P.* FROM acars.POSITIONS P "
              + "WHERE (P.FLIGHT_ID=?)");
        _ps.setInt(1, flightID);
        executeUpdate(0);
        
        // Delete the existing flight data
        prepareStatementWithoutLimits("DELETE FROM acars.POSITIONS WHERE (FLIGHT_ID=?)");
        _ps.setInt(1, flightID);
        executeUpdate(0);
        
        // Mark the flight as archived
        prepareStatement("UPDATE acars.FLIGHTS SET ARCHIVED=?, PIREP=? WHERE (ID=?)");
        _ps.setBoolean(1, true);
        _ps.setBoolean(2, true);
        _ps.setInt(3, flightID);
        executeUpdate(0);
        
        // Commit the transaction
        commitTransaction();
     } catch (SQLException se) {
        rollbackTransaction();
        throw new DAOException(se);
     }
   }
}