// Copyright 2005, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.stats.HTTPStatistics;

/**
 * A Data Access Object to write HTTP statistics.
 * @author Luke
 * @version 5.0
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
         _ps.setLong(4, stats.getExecutionTime());
         _ps.setLong(5, stats.getBandwidth());
         executeUpdate(1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}