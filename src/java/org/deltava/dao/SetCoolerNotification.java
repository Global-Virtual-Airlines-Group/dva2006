// Copyright 2005, 2008, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

/**
 * A Data Access Object to update Water Cooler thread notification entries.
 * @author Luke
 * @version 9.0
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
	   try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.COOLER_NOTIFY WHERE (THREAD_ID=?)")) {
         ps.setInt(1, threadID);
         executeUpdate(ps, 0);
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
	   try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.COOLER_NOTIFY (THREAD_ID, USER_ID, CREATED) VALUES (?, ?, NOW())")) {
         ps.setInt(1, threadID);
         ps.setInt(2, userID);
         executeUpdate(ps, 1);
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
	   try (PreparedStatement ps = prepare("DELETE FROM common.COOLER_NOTIFY WHERE (THREAD_ID=?) AND (USER_ID=?)")) {
         ps.setInt(1, threadID);
         ps.setInt(2, userID);
         executeUpdate(ps, 0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}