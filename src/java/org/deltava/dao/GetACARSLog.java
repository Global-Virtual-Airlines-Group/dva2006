// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.TextMessage;

/**
 * A Data Access Object to load ACARS log data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetACARSLog extends GetACARSData {

   /**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
    */
   public GetACARSLog(Connection c) {
      super(c);
   }

   /**
    * Returns all ACARS connection log entries for a particular User.
    * @param userID the User's Database ID
    * @return a List of ConnectionEntry beans sorted by date
    * @throws DAOException if a JDBC error occurs
    */
   public List getConnections(int userID) throws DAOException {
      try {
         prepareStatement("SELECT ID, PILOT_ID, DATE, INET_NTOA(REMOTE_ADDR), REMOTE_HOST FROM "
         		+ "acars.CONS WHERE (PILOT_ID=?) ORDER BY DATE DESC");
         _ps.setInt(1, userID);
         return executeConnectionInfo();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Returns all ACARS connection log entries between two dates.
    * @param sd the start date/time, or null
    * @param ed the end date/time or null
    * @return a List of ConnectionEntry beans sorted by date
    * @throws DAOException if a JDBC error occurs
    */
   public List getConnections(java.util.Date sd, java.util.Date ed) throws DAOException {
      
      // Clean up dates
      convertDate(sd, 1);
      convertDate(ed, System.currentTimeMillis());
      
      try {
         prepareStatement("SELECT ID, PILOT_ID, DATE, INET_NTOA(REMOTE_ADDR), REMOTE_HOST FROM "
         		+ "acars.CONS WHERE (DATE >= ?) AND (DATE <= ?) ORDER BY DATE DESC");
         _ps.setTimestamp(1, createTimestamp(sd));
         _ps.setTimestamp(2, createTimestamp(ed));
         return executeConnectionInfo();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Returns all ACARS text messages for a particular User.
    * @param userID the User's database ID
    * @return a List of TextMessage beans
    * @throws DAOException if a JDBC error occurs
    */
   public List getMessages(int userID) throws DAOException {
      try {
         prepareStatement("SELECT * FROM acars.MESSAGES WHERE (AUTHOR_ID=?) OR (RECIPENT_ID=?) ORDER BY DATE");
         _ps.setInt(1, userID);
         _ps.setInt(2, userID);
         return executeMsg();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Returns all ACARS text messages sent between two dates.
    * @param sd the start date/time, or null
    * @param ed the end date/time, or null
    * @return a List of TextMessage beans, sorted by date
    * @throws DAOException if a JDBC error occurs
    */
   public List getMessages(java.util.Date sd, java.util.Date ed) throws DAOException {
      
      // Clean up dates
      convertDate(sd, 1);
      convertDate(ed, System.currentTimeMillis());
      
      try {
         prepareStatement("SELECT * FROM acars.MESSAGES WHERE (DATE >= ?) AND (DATE <=?) ORDER BY DATE");
         _ps.setTimestamp(1, createTimestamp(sd));
         _ps.setTimestamp(2, createTimestamp(ed));
         return executeMsg();
      } catch (SQLException se) {
         throw new DAOException(se);
      }      
   }
   
   /**
    * Returns all Flight Information entries for a particular pilot.
    * @param pilotID the Pilot database ID
    * @return a List of InfoEntry beans
    * @throws DAOException if a JDBC error occurs
    */
   public List getFlights(int pilotID) throws DAOException {
      try {
         prepareStatement("SELECT F.*, C.PILOT_ID FROM acars.CONS C, acars.FLIGHTS F WHERE (C.ID=F.CON_ID) "
               + "AND (C.PILOT_ID=?) ORDER BY F.CREATED");
         _ps.setInt(1, pilotID);
         return executeFlightInfo();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Returns all Flight Information entries between two dates.
    * @param sd the start date/time, or null
    * @param ed the end date/time, or null
    * @return a List of InfoEntry beans
    * @throws DAOException if a JDBC error occurs
    */
   public List getFlights(java.util.Date sd, java.util.Date ed) throws DAOException {
      
      // Clean up dates
      convertDate(sd, 1);
      convertDate(ed, System.currentTimeMillis());
      
      try {
         prepareStatement("SELECT F.*, C.PILOT_ID FROM acars.FLIGHTS F, acars.CONS C WHERE (C.ID=F.CON_ID) "
               + " AND (F.CREATED >=?) AND (CREATED <=?) ORDER BY F.CREATED");
         _ps.setTimestamp(1, createTimestamp(sd));
         _ps.setTimestamp(2, createTimestamp(ed));
         return executeFlightInfo();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Helper method to parse Message result sets.
    */
   private List executeMsg() throws SQLException {
      
      // Execute the query
      ResultSet rs = _ps.executeQuery();
      
      // Iterate through the requests
      List results = new ArrayList();
      while (rs.next()) {
         TextMessage msg = new TextMessage(rs.getLong(1), rs.getString(6));
         msg.setConnectionID(rs.getLong(2));
         msg.setDate(rs.getTimestamp(3));
         msg.setAuthorID(rs.getInt(4));
         msg.setRecipientID(rs.getInt(5));
         
         // Add to results
         results.add(msg);
      }
      
      // Clean up and return
      rs.close();
      _ps.close();
      return results;
   }
}