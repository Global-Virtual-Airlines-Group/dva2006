// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.EMailConfiguration;

/**
 * A Data Access Object to load Pilot IMAP mailbox information.
 * @author Luke
 * @version 2.2
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
    * Retrieves IMAP e-mail data about a particular user.
    * @param id the Pilot's database ID
    * @return the EMailConfiguration bean, or null if not found
    * @throws DAOException if a JDBC error occurs
    */
   public EMailConfiguration getEMailInfo(int id) throws DAOException {
       try {
           prepareStatementWithoutLimits("SELECT ID, username, maildir, quota, active FROM postfix.mailbox "
        		   + "WHERE (ID=?) LIMIT 1");
           _ps.setInt(1, id);
           
           // Execute the query, return null if not found
           List<EMailConfiguration> results = execute();
           return (results.isEmpty()) ? null : results.get(0);
       } catch (SQLException se) {
           throw new DAOException(se);
       }
   }
   
   /**
    * Returns all IMAP mailbox profiles.
    * @return a Collection of EMailConfiguration beans
    * @throws DAOException if a JDBC error occurs
    */
   public Collection<EMailConfiguration> getAll() throws DAOException {
      try {
         prepareStatementWithoutLimits("SELECT ID, username, maildir, quota, active FROM postfix.mailbox "
        		 + "WHERE (ID>0)");
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