// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.HelpEntry;

/**
 * A Data Access Object to read Online Help entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetHelpEntry extends DAO {

   /**
    * Initialize the Data Access Object.
    * @param c the JDBC connection to use
    */
   public GetHelpEntry(Connection c) {
      super(c);
   }

   /**
    * Returns a particular Online Help entry.
    * @param name the entry name
    * @return a HelpEntry bean, or null if not found
    * @throws DAOException if a JDBC error occurs
    */
   public HelpEntry get(String name) throws DAOException {
      try {
         setQueryMax(1);
         prepareStatement("SELECT * FROM HELP WHERE (ID=?)");
         _ps.setString(1, name);
         
         // Execute the query, return null if empty
         List<HelpEntry> results = execute();
         return results.isEmpty() ? null : results.get(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Returns all Online Help entries.
    * @return a List of HelpEntry beans
    * @throws DAOException if a JDBC error occurs
    */
   public List<HelpEntry> getAll() throws DAOException {
      try {
         prepareStatement("SELECT * FROM HELP ORDER BY ID");
         return execute();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Helper method to parse the result set.
    */
   private List<HelpEntry> execute() throws SQLException {

      // Execute the query
      ResultSet rs = _ps.executeQuery();

      // Iterate through the results
      List<HelpEntry> results = new ArrayList<HelpEntry>();
      while (rs.next()) {
         HelpEntry e = new HelpEntry(rs.getString(1), rs.getString(2));
         results.add(e);
      }

      // Clean up and return
      rs.close();
      _ps.close();
      return results;
   }
}