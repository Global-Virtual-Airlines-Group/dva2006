// Copyright 2005, 2010, 2011, 2014, 2016, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.*;
import org.deltava.util.MailUtils;

/**
 * A Data Access Object to get Pilot notification lists.
 * @author Luke
 * @version 9.0
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

   /**
    * Returns Pilots signed up to receive a particular notification type.
    * @param notificationType the Notification Type to filter on
    * @return a List of objects implementing {@link EMailAddress}
    * @throws DAOException if a JDBC error occurs
    * @throws IllegalArgumentException if notificationType is invalid
    */
   public List<EMailAddress> getNotifications(Notification notificationType) throws DAOException {
	   try (PreparedStatement ps = prepare("SELECT CONCAT_WS(' ', FIRSTNAME, LASTNAME) AS NM, EMAIL FROM PILOTS WHERE (STATUS=?) AND ((NOTIFY & ?) > 0) ORDER BY LASTNAME, FIRSTNAME")) {
         ps.setInt(1, Pilot.ACTIVE);
         ps.setInt(2, notificationType.getCode());
         
         // Execute the query
         List<EMailAddress> results = new ArrayList<EMailAddress>();
         try (ResultSet rs = ps.executeQuery()) {
        	 while (rs.next())
        		 results.add(MailUtils.makeAddress(rs.getString(2), rs.getString(1)));	
         }
         
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
	   try (PreparedStatement ps = prepare("SELECT ID FROM PILOTS WHERE (STATUS=?) AND ((NOTIFY & ?) > 0)")) {
		   ps.setInt(1, Pilot.ACTIVE);
		   ps.setInt(2, notificationType.getCode());
		   return getByID(executeIDs(ps), "PILOTS").values();
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }
}