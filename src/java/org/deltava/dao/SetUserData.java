// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.UserData;

/**
 * A Data Access Object to write cross-applicaton User data. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @deprecated
 */

public class SetUserData extends DAO {

   /**
    * Initialize the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetUserData(Connection c) {
      super(c);
   }

   /**
    * Writes a User Data entry to the database.
    * @param usr the UserData object
    * @throws DAOException if a JDBC error occurs
    */
   public void write(UserData usr) throws DAOException {
      try {
         prepareStatement("INSERT INTO common.USERDATA (ID, AIRLINE, DBNAME, TABLENAME, DOMAIN) "
               + "VALUES (?, ?, ?, ?, ?)");
         _ps.setInt(1, usr.getID());
         _ps.setString(2, usr.getAirlineCode());
         _ps.setString(3, usr.getDB());
         _ps.setString(4, usr.getTable());
         _ps.setString(5, usr.getDomain());
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}