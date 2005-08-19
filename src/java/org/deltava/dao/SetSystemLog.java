// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.stats.HTTPStatistics;

/**
 * A Data Access Object to write HTTP statistics and purge System Log entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetSystemLog extends DAO {

   /**
    * Initializes the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetSystemLog(Connection c) {
      super(c);
   }

   /**
    * Purges all Log Entries before a particular Date.
    * @param logName the log name (APPS or TASKS)
    * @param pd the purge date/time
    * @return the number of entries purged
    * @throws DAOException if a JDBC error occurs
    */
   public int purge(String logName, java.util.Date pd) throws DAOException {
      return purge(logName, pd, 10000000);
   }
   
   /**
    * Purges all Log Entries with a particular priority <i>or lower</i> before a particular Date.
    * @param logName the log name (APPS or TASKS)
    * @param pd the purge date/time
    * @param priority the priority code
    * @return the number of entries purged
    * @throws DAOException if a JDBC error occurs
    */
   public int purge(String logName, java.util.Date pd, int priority) throws DAOException {
      try {
         prepareStatement("DELETE FROM LOG_" + logName + " WHERE (PRIORITY <= ?) AND "
               + "(CREATED <= ?)");
         _ps.setInt(1, priority);
         _ps.setTimestamp(2, createTimestamp(pd));
         
         // Update and return back the number of rows purged
         int rowsUpdated = _ps.executeUpdate();
         _ps.close();
         return rowsUpdated;
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Writes an HTTP statistics bean to the log database. This handles inserts and updates.
    * @param stats the statistics bean
    * @throws DAOException if a JDBC error occurs
    */
   public void write(HTTPStatistics stats) throws DAOException {
      try {
         prepareStatement("REPLACE INTO SYS_HTTPLOG (DATE, REQUESTS, HOMEHITS, EXECTIME, BANDWIDTH) "
               + "VALUES (?, ?, ?, ?, ?)");
         _ps.setTimestamp(1, createTimestamp(stats.getDate()));
         _ps.setInt(2, stats.getRequests());
         _ps.setInt(3, stats.getHomePageHits());
         _ps.setInt(4, stats.getExecutionTime());
         _ps.setLong(5, stats.getBandwidth());
         executeUpdate(1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}