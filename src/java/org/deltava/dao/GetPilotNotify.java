// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.*;

/**
 * A Data Access Object to get Pilot notification lists.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetPilotNotify extends PilotReadDAO {

   /**
    * Initialize the Data Access Object.
    * @param c the JDBC connection to use
    */
   public GetPilotNotify(Connection c) {
      super(c);
   }

   public class EMailNotificationImpl implements EMailAddress {
      
      private String _name;
      private String _addr;
      
      EMailNotificationImpl(String firstName, String lastName, String addr) {
         super();
         _name = firstName + " " + lastName;
         _addr = addr;
      }
      
      public String getName() {
         return _name;
      }
      
      public String getEmail() {
         return _addr;
      }
   }
   
   /**
    * Returns Pilots signed up to receive a particular notification type.
    * @param notificationType the Notification Type to filter on.
    * @return a List of objects implementing {@link EMailAddress}
    * @throws DAOException if a JDBC error occurs
    * @throws IllegalArgumentException if notificationType is invalid
    */
   public List<EMailAddress> getNotifications(String notificationType) throws DAOException {
      
      // Figure out the database field
      String fieldName = "";
      if (Person.EVENT.equals(notificationType)) {
         fieldName = "EVENT_NOTIFY";
      } else if (Person.FLEET.equals(notificationType)) {
         fieldName = "FILE_NOTIFY";
      } else if (Person.NEWS.equals(notificationType)) {
         fieldName = "NEWS_NOTIFY";
      } else {
         throw new IllegalArgumentException("Invalid notification type - " + notificationType);
      }
      
      // Build the SQL statement
      StringBuilder sqlBuf = new StringBuilder("SELECT FIRSTNAME, LASTNAME, EMAIL FROM PILOTS WHERE "
            + "(STATUS=?) AND (" + fieldName + "=?) ORDER BY LASTNAME, FIRSTNAME");
      
      try {
         prepareStatement(sqlBuf.toString());
         _ps.setInt(1, Pilot.ACTIVE);
         _ps.setBoolean(2, true);
         
         // Execute the query
         List<EMailAddress> results = new ArrayList<EMailAddress>();
         ResultSet rs = _ps.executeQuery();
         while (rs.next()) {
            EMailAddress email = new EMailNotificationImpl(rs.getString(1), rs.getString(2), rs.getString(3));
            results.add(email);
         }
         
         // Clean up and return
         rs.close();
         _ps.close();
         return results;
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}