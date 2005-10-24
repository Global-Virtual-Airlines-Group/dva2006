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
         prepareStatement("DELETE FROM acars.FLIGHTS WHERE (ID=?)");
         _ps.setInt(1, flightID);
         executeUpdate(0);
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
        prepareStatementWithoutLimits("INSERT INTO acars.POSITION_ARCHIVE ARC SELECT P.* FROM acars.POSITIONS P "
              + "WHERE (P.FLIGHT_ID=?)");
        _ps.setInt(1, flightID);
        executeUpdate(0);
        
        // Delete the existing flight data
        prepareStatementWithoutLimits("DELETE FROM acars.POSITIONS WHERE (FLIGHT_ID=?)");
        _ps.setInt(1, flightID);
        executeUpdate(0);
        
        // Mark the flight as archived
        prepareStatement("UPDATE acars.FLIGHTS SET ARCHIVED=? WHERE (ID=?)");
        _ps.setBoolean(1, true);
        _ps.setInt(2, flightID);
        executeUpdate(0);
        
        // Commit the transaction
        commitTransaction();
     } catch (SQLException se) {
        rollbackTransaction();
        throw new DAOException(se);
     }
   }
}