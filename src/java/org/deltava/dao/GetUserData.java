// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.system.UserData;
import org.deltava.beans.system.UserDataMap;

import org.deltava.util.CollectionUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to load cross-application User data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetUserData extends DAO {

   private static final Logger log = Logger.getLogger(GetUserData.class);

   static final Cache _cache = new AgingCache(160); // Package private so the set DAO can update it

   /**
    * Initializes the Data Access Object.
    * @param c the JDBC connection to use
    */
   public GetUserData(Connection c) {
      super(c);
   }

   /**
    * Returns cross-application data for a particular database ID.
    * @param id the User's database ID
    * @return the UserData object for that user, or null if not found
    * @throws DAOException if a JDBC error occurs
    */
   public UserData get(int id) throws DAOException {
      try {
         prepareStatement("SELECT * FROM common.USERDATA WHERE (ID=?)");
         _ps.setInt(1, id);
         setQueryMax(1);

         // Get the results, if empty return null
         List results = execute();
         return results.isEmpty() ? null : (UserData) results.get(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Returns cross-application data for a particular Water Cooler message thread.
    * @param threadID the Message Thread database ID
    * @return a UserDataMap
    * @throws DAOException if a JDBC error occurs
    */
   public UserDataMap getByThread(int threadID) throws DAOException {
      try {
         prepareStatement("SELECT * FROM common.USERDATA UD LEFT JOIN common.COOLER_POSTS P ON "
         		+ "(P.AUTHOR_ID=UD.ID) WHERE (P.THREAD_ID=?)");
         _ps.setInt(1, threadID);
         return new UserDataMap(execute());
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Returns cross-application data for a particular Online Event.
    * @param eventID the Online Event database ID
    * @return a UserDataMap
    * @throws DAOException if a JDBC error occurs
    */
   public UserDataMap getByEvent(int eventID) throws DAOException {
      try {
         prepareStatement("SELECT * FROM common.USERDATA UD LEFT JOIN common.EVENT_SIGNUPS ES ON "
         		+ "(ES.PILOT_ID=UD.ID) WHERE (ES.ID=?)");
         _ps.setInt(1, eventID);
         return new UserDataMap(execute());
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Returns cross-application data for a Set of User IDs.
    * @param ids a Set of Integers with user IDs
    * @return a UserDataMap
    * @throws DAOException if a JDBC error occurs
    */
   public UserDataMap get(Set ids) throws DAOException {

      // Build the SQL statement
      StringBuffer sqlBuf = new StringBuffer("SELECT * FROM common.USERDATA WHERE ID IN (");

      // Strip out entries already in the cache
      log.debug("Raw set size = " + ids.size());
      int querySize = 0;
      UserDataMap result = new UserDataMap();
      for (Iterator i = ids.iterator(); i.hasNext();) {
         Integer id = (Integer) i.next();

         // Pull from the cache if at all possible; this is an evil query
         UserData usr = (UserData) _cache.get(id);
         if (usr == null) {
            querySize++;
            sqlBuf.append(id.toString());
            if (i.hasNext()) sqlBuf.append(',');
         } else {
            result.put(null, usr);
         }
      }

      // Only execute the prepared statement if we haven't gotten anything from the cache
      log.debug("Uncached set size = " + querySize);
      if (querySize > 0) {
         if (sqlBuf.charAt(sqlBuf.length() - 1) == ',') sqlBuf.setLength(sqlBuf.length() - 1);
         sqlBuf.append(')');
         
         // Execute the query
         setQueryMax(querySize);
         try {
            prepareStatement(sqlBuf.toString());
            result.putAll(CollectionUtils.createMap(execute(), "ID"));
         } catch (SQLException se) {
            throw new DAOException(se);
         }
      }

      // Return the result container
      return result;
   }

   /**
    * Helper method to iterate through the result set.
    */
   private List execute() throws SQLException {

      // Execute the query
      ResultSet rs = _ps.executeQuery();

      // Iterate through the results
      List results = new ArrayList();
      while (rs.next()) {
         UserData usr = new UserData(rs.getInt(1));
         usr.setAirlineCode(rs.getString(2));
         usr.setDB(rs.getString(3));
         usr.setTable(rs.getString(4));
         usr.setDomain(rs.getString(5));

         // Add to results and the cache
         results.add(usr);
         _cache.add(usr);
      }

      // Clean up and return
      rs.close();
      _ps.close();
      return results;
   }
}