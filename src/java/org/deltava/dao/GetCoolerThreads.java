package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.cooler.*;

/**
 * A Data Access Object to retrieve Water Cooler threads.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetCoolerThreads extends DAO {

   /**
    * Initializes the Data Access Object.
    * @param c the JDBC connection to use
    */
   public GetCoolerThreads(Connection c) {
      super(c);
   }

   /**
    * Get all Water Cooler threads from a particular Channel.
    * @param channelName the Channle name
    * @param showImgs TRUE if screen shot threads should be included, otherwise FALSE
    * @return a List of MessageThread beans
    * @throws DAOException if a JDBC error occurs
    */
   public List getByChannel(String channelName, boolean showImgs) throws DAOException {

      // Build the SQL statement
      StringBuffer sqlBuf = new StringBuffer("SELECT T.*, IFNULL(T.STICKY, T.LASTUPDATE) AS SD FROM "
            + "common.COOLER_THREADS T WHERE (T.CHANNEL=?)");
      if (!showImgs)
         sqlBuf.append(" AND (T.IMAGE_ID=0)");
      sqlBuf.append(" ORDER BY SD DESC");

      try {
         prepareStatement(sqlBuf.toString());
         _ps.setString(1, channelName);
         return execute();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Get all Water Cooler threads from a particular Author.
    * @param userID the Author's database ID
    * @param showImgs TRUE if screen shot threads should be included, otherwise FALSE
    * @return a List of MessageThread beans
    * @throws DAOException if a JDBC error occurs
    */
   public List getByAuthor(int userID, boolean showImgs) throws DAOException {

      // Build the SQL statement
      StringBuffer sqlBuf = new StringBuffer("SELECT T.*, IFNULL(T.STICKY, T.LASTUPDATE) AS SD FROM "
            + "common.COOLER_THREADS T WHERE (T.AUTHOR=?)");
      if (!showImgs)
         sqlBuf.append(" AND (T.IMAGE_ID=0)");
      sqlBuf.append(" ORDER BY SD DESC");
      
      try {
         prepareStatement(sqlBuf.toString());
         _ps.setInt(1, userID);
         return execute();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Get all Water Cooler threads.
    * @param showImgs TRUE if screen shot threads should be included, otherwise FALSE
    * @return a List of MessageThreads
    * @throws DAOException if a JDBC error occurs
    */
   public List getAll(boolean showImgs) throws DAOException {

      // Build the SQL statement
      StringBuffer sqlBuf = new StringBuffer("SELECT T.*, IFNULL(T.STICKY, T.LASTUPDATE) AS SD FROM "
            + "common.COOLER_THREADS T ");

      if (!showImgs)
         sqlBuf.append(" WHERE (T.IMAGE_ID=0)");
      sqlBuf.append(" GROUP BY T.ID ORDER BY SD DESC");

      try {
         prepareStatement(sqlBuf.toString());
         return execute();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Returns all Water Cooler threads updated since a particular date.
    * @param sd the date/time
    * @param showImgs TRUE if screen shot threads should be included, otherwise FALSE
    * @return a List of MessageThreads
    * @throws DAOException if a JDBC error occurs
    */
   public List getSince(java.util.Date sd, boolean showImgs) throws DAOException {
      if (sd == null)
         return getAll(showImgs);

      // Build the SQL statement
      StringBuffer sqlBuf = new StringBuffer("SELECT T.*, IFNULL(T.STICKY, T.LASTUPDATE) AS SD FROM "
            + "common.COOLER_THREADS T");
      if (!showImgs)
         sqlBuf.append(" WHERE (T.IMAGE_ID=0) ");
      
      sqlBuf.append("HAVING (SD > ?) ORDER BY SD DESC");

      try {
         prepareStatement(sqlBuf.toString());
         _ps.setTimestamp(1, createTimestamp(sd));
         return execute();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Retrieves a particular discussion thread.
    * @param id the thread ID
    * @return a MessageThread
    * @throws DAOException
    */
   public MessageThread getThread(int id) throws DAOException {
      try {
         prepareStatement("SELECT * FROM common.COOLER_THREADS WHERE (ID=?)");
         _ps.setInt(1, id);

         // Execute the query - if id not found return null
         ResultSet rs = _ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            return null;
         }

         // Populate the thread data
         MessageThread t = new MessageThread(rs.getString(2));
         t.setID(id);
         t.setChannel(rs.getString(3));
         t.setImage(rs.getInt(4));
         t.setStickyUntil(rs.getTimestamp(5));
         t.setHidden(rs.getBoolean(6));
         t.setLocked(rs.getBoolean(7));
         t.setStickyInChannelOnly(rs.getBoolean(8));
         t.setViews(rs.getInt(9));

         // Clean up
         rs.close();
         _ps.close();

         // Fetch the thread posts
         prepareStatementWithoutLimits("SELECT THREAD_ID, POST_ID, AUTHOR_ID, CREATED, INET_NTOA(REMOTE_ADDR), "
               + "REMOTE_HOST, MSGBODY FROM common.COOLER_POSTS WHERE (THREAD_ID=?) ORDER BY CREATED");
         _ps.setInt(1, id);

         // Execute the query
         rs = _ps.executeQuery();
         while (rs.next()) {
            Message msg = new Message(rs.getInt(3));
            msg.setThreadID(id);
            msg.setID(rs.getInt(2));
            msg.setCreatedOn(rs.getTimestamp(4));
            msg.setRemoteAddr(rs.getString(5));
            msg.setRemoteHost(rs.getString(6));
            msg.setBody(rs.getString(7));
            t.addPost(msg);
         }

         // Clean up
         rs.close();
         _ps.close();
         return t;
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Returns all Message Threads matching particular search criteria.
    * @param searchStr the search term
    * @param channelName an optional Channel name
    * @return a List of MessageThreads
    * @throws DAOException if a JDBC error occurs
    */
   public List search(String searchStr, String channelName) throws DAOException {
      try {
         prepareStatement("SELECT T.*, IFNULL(T.STICKY, T.LASTUPDATE) AS SD FROM common.COOLER_THREADS T "
               + "LEFT JOIN common.COOLER_POSTS P ON (T.ID=P.THREAD_ID) WHERE (T.CHANNEL=?) AND (P.MSGBODY LIKE ?) "
               + "ORDER BY SD DESC");
         _ps.setString(1, channelName);
         _ps.setString(2, "%" + searchStr + "%");
         return execute();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Helper method to load result rows.
    */
   private List execute() throws SQLException {
      List results = new ArrayList();

      // Execute the query
      ResultSet rs = _ps.executeQuery();
      while (rs.next()) {
         MessageThread t = new MessageThread(rs.getString(2));
         t.setID(rs.getInt(1));
         t.setChannel(rs.getString(3));
         t.setImage(rs.getInt(4));
         t.setStickyUntil(rs.getTimestamp(5));
         t.setHidden(rs.getBoolean(6));
         t.setLocked(rs.getBoolean(7));
         t.setStickyInChannelOnly(rs.getBoolean(8));
         t.setViews(rs.getInt(9));
         t.setPostCount(rs.getInt(10));
         t.setAuthorID(rs.getInt(11));
         t.setLastUpdatedOn(rs.getTimestamp(12));
         t.setLastUpdateID(rs.getInt(13));

         // Add to results
         results.add(t);
      }

      // Clean up and return
      rs.close();
      _ps.close();
      return results;
   }
}