// Copyright 2005, 2010, 2011, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.*;

/**
 * A Data Access Object to get Pilot notification lists.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GetPilotNotify extends GetPilot {

   /**
    * Initialize the Data Access Object.
    * @param c the JDBC connection to use
    */
   public GetPilotNotify(Connection c) {
      super(c);
   }

   public class EMailNotificationImpl implements EMailAddress {
      
      private final String _name;
      private final String _addr;
      
      protected EMailNotificationImpl(String firstName, String lastName, String addr) {
         super();
         _addr = addr;
         _name = firstName + " " + lastName;
      }
      
      @Override
      public String getName() {
         return _name;
      }
      
      @Override
      public String getEmail() {
         return _addr;
      }
      
      @Override
      public boolean isInvalid() {
    	  return false;
      }
      
      @Override
      public int hashCode() {
    	  return _addr.hashCode();
      }
      
      @Override
      public boolean equals(Object o) {
    	  return (o instanceof EMailAddress) ? _addr.equals(((EMailAddress) o).getEmail()) : false; 
      }
   }
   
   /**
    * Returns Pilots signed up to receive a particular notification type.
    * @param notificationType the Notification Type to filter on
    * @return a List of objects implementing {@link EMailAddress}
    * @throws DAOException if a JDBC error occurs
    * @throws IllegalArgumentException if notificationType is invalid
    */
   public List<EMailAddress> getNotifications(Notification notificationType) throws DAOException {
      try {
         prepareStatement("SELECT FIRSTNAME, LASTNAME, EMAIL FROM PILOTS WHERE (STATUS=?) AND "
        		 + "((NOTIFY & ?) > 0) ORDER BY LASTNAME, FIRSTNAME");
         _ps.setInt(1, Pilot.ACTIVE);
         _ps.setInt(2, notificationType.getCode());
         
         // Execute the query
         List<EMailAddress> results = new ArrayList<EMailAddress>();
         try (ResultSet rs = _ps.executeQuery()) {
        	 while (rs.next())
            results.add(new EMailNotificationImpl(rs.getString(1), rs.getString(2), rs.getString(3)));	
         }
         
         _ps.close();
         return results;
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Returns the populated Pilots signed up to receive a particulr notification type.
    * @param notificationType the Notification Type to filter on.
    * @return a Collection of Pilot beans
    * @throws DAOException if a JDBC error occurs
    */
   public Collection<Pilot> gePilots(Notification notificationType) throws DAOException {
	   try {
		   prepareStatement("SELECT ID FROM PILOTS WHERE (STATUS=?) AND ((NOTIFY & ?) > 0)");
		   _ps.setInt(1, Pilot.ACTIVE);
		   _ps.setInt(2, notificationType.getCode());
		   return getByID(executeIDs(), "PILOTS").values();
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }
}