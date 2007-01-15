// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.EMailConfiguration;

/**
 * A Data Access Object to load Pilot IMAP mailbox information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetPilotEMail extends DAO {

   /**
    * Initailizes the Data Access Object.
    * @param c the JDBC connection to use
    */
   public GetPilotEMail(Connection c) {
      super(c);
   }
   
   /**
    * Returns if a particular SMTP mailbox address is available.
    * @param addr the proposed e-mail address
    * @return TRUE if the address is available, otherwise FALSE
    * @throws DAOException if a JDBC error occurs
    */
   public boolean isAvailable(String addr) throws DAOException {
      try {
         prepareStatement("SELECT COUNT(*) FROM postfix.mailbox WHERE (username=?)");
         _ps.setString(1, addr);
         
         // Execute the query and get result
         ResultSet rs = _ps.executeQuery();
         boolean result = rs.next() ? (rs.getInt(1) == 0) : true;
         
         // Clean up and return
         rs.close();
         _ps.close();
         return result;
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Retrieves IMAP e-mail data about a particular user.
    * @param id the Pilot's database ID
    * @return the EMailConfiguration bean, or null if not found
    * @throws DAOException if a JDBC error occurs
    */
   public EMailConfiguration getEMailInfo(int id) throws DAOException {
       try {
    	   setQueryMax(1);
           prepareStatement("SELECT ID, username, maildir, quota, active FROM postfix.mailbox WHERE (ID=?)");
           _ps.setInt(1, id);
           
           // Execute the query, return null if not found
           List results = execute();
           setQueryMax(0);
           return (results.isEmpty()) ? null : (EMailConfiguration) results.get(0);
       } catch (SQLException se) {
           throw new DAOException(se);
       }
   }
   
   /**
    * Returns all IMAP mailbox profiles.
    * @return a Collection of EMailConfiguration beans
    * @throws DAOException if a JDBC error occurs
    */
   public Collection getAll() throws DAOException {
      try {
         prepareStatement("SELECT ID, username, maildir, quota, active FROM postfix.mailbox");
         return execute();
      } catch (SQLException se) {
         throw new DAOException(se); 
      }
   }
   
   /**
    * Helper method to load EMail information.
    */
   private List<EMailConfiguration> execute() throws SQLException {
      
      // Execute the Query
      Map<String, EMailConfiguration> results = new HashMap<String, EMailConfiguration>();
      ResultSet rs = _ps.executeQuery();
      while (rs.next()) {
         EMailConfiguration result = new EMailConfiguration(rs.getInt(1), rs.getString(2));
         result.setMailDirectory(rs.getString(3));
         result.setQuota(rs.getInt(4));
         result.setActive(rs.getBoolean(5));

         // Add to result map
         results.put(result.getAddress(), result);
      }
      
      // Clean up result set
      rs.close();
      _ps.close();

      // If we've retrieved nothing, exit
      if (results.isEmpty())
    	  return Collections.emptyList();

      // Build SQL statement
      StringBuilder sqlBuf = new StringBuilder("SELECT goto, address FROM postfix.alias WHERE (goto IN (");
      for (Iterator i = results.keySet().iterator(); i.hasNext(); ) {
         String addr = (String) i.next();
         sqlBuf.append("\'");
         sqlBuf.append(addr);
         sqlBuf.append(i.hasNext() ? "\'," : "\'))");
      }
      
      // Fetch aliases
      prepareStatementWithoutLimits(sqlBuf.toString());
      rs = _ps.executeQuery();
      while (rs.next()) {
         EMailConfiguration cfg = results.get(rs.getString(1));
         if (cfg != null)
            cfg.addAlias(rs.getString(2));
      }
      
      // Clean up and return
      rs.close();
      _ps.close();
      return new ArrayList<EMailConfiguration>(results.values());
   }
}