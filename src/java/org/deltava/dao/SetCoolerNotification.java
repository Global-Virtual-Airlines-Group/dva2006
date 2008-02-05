// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

/**
 * A Data Access Object to update Water Cooler thread notification entries.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class SetCoolerNotification extends DAO {

   /**
    * Initializes the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetCoolerNotification(Connection c) {
      super(c);
   }

   /**
    * Clears all notification entries for a particular message thread.
    * @param threadID the message thread database ID
    * @throws DAOException if a JDBC error occurs
    */
   public void clear(int threadID) throws DAOException {
      try {
         prepareStatementWithoutLimits("DELETE FROM common.COOLER_NOTIFY WHERE (THREAD_ID=?)");
         _ps.setInt(1, threadID);
         executeUpdate(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Adds a notification entry for a particular message thread.
    * @param threadID the message thread database ID
    * @param userID the user database ID
    * @throws DAOException if a JDBC error occurs
    */
   public void add(int threadID, int userID) throws DAOException {
      try {
         prepareStatement("REPLACE INTO common.COOLER_NOTIFY (THREAD_ID, USER_ID, CREATED) VALUES (?, ?, NOW())");
         _ps.setInt(1, threadID);
         _ps.setInt(2, userID);
         executeUpdate(1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Removes a notification entry for a particular message thread.
    * @param threadID the message thread database ID
    * @param userID the user database ID
    * @throws DAOException if a JDBC error occurs
    */
   public void delete(int threadID, int userID) throws DAOException {
      try {
         prepareStatement("DELETE FROM common.COOLER_NOTIFY WHERE (THREAD_ID=?) AND (USER_ID=?)");
         _ps.setInt(1, threadID);
         _ps.setInt(2, userID);
         executeUpdate(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}