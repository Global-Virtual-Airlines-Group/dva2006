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
         prepareStatement("SELECT C.ID, C.PILOT_ID, C.DATE, INET_NTOA(C.REMOTE_ADDR), C.REMOTE_HOST, "
         		+ "C.CLIENT_BUILD, COUNT(DISTINCT F.ID), COUNT(DISTINCT M.ID), COUNT(P.CON_ID) FROM acars.CONS C "
         		+ "LEFT JOIN acars.FLIGHTS F ON (C.ID=F.CON_ID) LEFT JOIN acars.MESSAGES M ON (C.ID=M.CON_ID) "
         		+ "LEFT JOIN acars.POSITIONS P ON (C.ID=P.CON_ID) WHERE (C.PILOT_ID=?) GROUP BY C.ID ORDER BY "
         		+ "C.DATE DESC");
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
         prepareStatement("SELECT C.ID, C.PILOT_ID, C.DATE, INET_NTOA(C.REMOTE_ADDR), C.REMOTE_HOST, "
               + "C.CLIENT_BUILD, COUNT(DISTINCT F.ID), COUNT(DISTINCT M.ID), COUNT(P.CON_ID) FROM acars.CONS C "
               + "LEFT JOIN acars.FLIGHTS F ON (C.ID=F.CON_ID) LEFT JOIN acars.MESSAGES M ON (C.ID=M.CON_ID) "
               + "LEFT JOIN acars.POSITIONS P ON (C.ID=P.CON_ID) WHERE (C.DATE >= ?) AND (C.DATE <= ?) "
               + "GROUP BY C.ID ORDER BY C.DATE DESC");
         _ps.setTimestamp(1, createTimestamp(sd));
         _ps.setTimestamp(2, createTimestamp(ed));
         return executeConnectionInfo();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Returns all ACARS connection log entries with no associated Flight Info logs or text messages. A cutoff interval 
    * is provided to prevent the accidental inclusion of flights still in progress.
    * @param cutoff the cutoff interval for connection entries, in hours
    * @return a List of ConnectionEntry beans sorted by date
    * @throws DAOException if a JDBC error occurs
    */
   public List getUnusedConnections(int cutoff) throws DAOException {
      try {
         prepareStatement("SELECT C.ID, C.PILOT_ID, C.DATE, INET_NTOA(C.REMOTE_ADDR), C.REMOTE_HOST, "
               + "C.CLIENT_BUILD, COUNT(DISTINCT M.ID) AS MC, COUNT(DISTINCT F.ID) AS FC, COUNT(P.CON_ID) AS PC "
               + "FROM acars.CONS C LEFT JOIN acars.FLIGHTS F ON (C.ID=F.CON_ID) LEFT JOIN acars.MESSAGES M ON "
               + "(C.ID=M.CON_ID) LEFT JOIN acars.POSITIONS P ON (C.ID=P.CON_ID) GROUP BY C.ID WHERE "
               + "(C.DATE < DATE_SUB(NOW(), INTERVAL ? HOUR) HAVING (MC=0) AND (FC=0) AND (PC=0) "
               + "ORDER BY C.DATE");
         _ps.setInt(1, cutoff);
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
         prepareStatement("SELECT * FROM acars.MESSAGES WHERE (AUTHOR=?) OR (RECIPIENT=?) ORDER BY DATE");
         _ps.setInt(1, userID);
         _ps.setInt(2, userID);
         return executeMsg();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Returns the latest ACARS text messages.
    * @return a List of TextMessage beans
    * @throws DAOException if a JDBC error occurs
    */
   public List getMessages() throws DAOException {
      try {
         prepareStatement("SELECT * FROM acars.MESSAGES ORDER BY DATE DESC");
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
         prepareStatement("SELECT M.* FROM acars.MESSAGES M WHERE (M.DATE >= ?) AND "
               + "(M.DATE <= ?) ORDER BY M.DATE");
         _ps.setTimestamp(1, createTimestamp(sd));
         _ps.setTimestamp(2, createTimestamp(ed));
         return executeMsg();
      } catch (SQLException se) {
         throw new DAOException(se);
      }      
   }
   
   /**
    * Returns the latest Flight Information entries.
    * @return a List of InfoEntry beans
    * @throws DAOException if a JDBC error occurs
    */
   public List getFlights() throws DAOException {
      try {
         prepareStatement("SELECT F.*, C.PILOT_ID FROM acars.CONS C, acars.FLIGHTS F WHERE (C.ID=F.CON_ID) "
               + "ORDER BY F.CREATED DESC");
         return executeFlightInfo();
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
               + " AND (F.CREATED >=?) AND (F.CREATED <=?) ORDER BY F.CREATED");
         _ps.setTimestamp(1, createTimestamp(sd));
         _ps.setTimestamp(2, createTimestamp(ed));
         return executeFlightInfo();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Returns all Flight Information entries without an associated Flight Report. A cutoff interval is provided to prevent
    * the accidental inclusion of flights still in progress.
    * @param cutoff the cutoff interval for flight entries, in hours
    * @return a List of InfoEntry beans sorted by date
    * @throws DAOException if a JDBC error occurs
    */
   public List getUnreportedFlights(int cutoff) throws DAOException {
     try {
        prepareStatement("SELECT F.*, C.PILOT_ID FROM acars.FLIGHTS F LEFT JOIN ACARS_PIREPS APR ON "
              + "(F.ID=APR.ACARS_ID) LEFT JOIN acars.CONS C ON (C.ID=F.CON_ID) WHERE (APR.ACARS_ID IS NULL) "
              + "AND (F.CREATED < DATE_SUB(NOW(), INTERVAL ? HOUR)) ORDER BY F.CREATED");
        _ps.setInt(1, cutoff);
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