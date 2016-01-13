// Copyright 2005, 2007, 2008, 2010, 2011, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.IMAPConfiguration;

/**
 * A Data Access Object to load Pilot IMAP mailbox information.
 * @author Luke
 * @version 6.4
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
   public IMAPConfiguration getEMailInfo(int id) throws DAOException {
       try {
           prepareStatementWithoutLimits("SELECT ID, username, maildir, quota, allow_smtp, active FROM postfix.mailbox WHERE (ID=?) LIMIT 1");
           _ps.setInt(1, id);
           
           // Execute the query, return null if not found
           List<IMAPConfiguration> results = execute();
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
   public Collection<IMAPConfiguration> getAll() throws DAOException {
      try {
         prepareStatement("SELECT ID, username, maildir, quota, allow_smtp, active FROM postfix.mailbox WHERE (ID>0) ORDER BY ID");
         return execute();
      } catch (SQLException se) {
         throw new DAOException(se); 
      }
   }
   
   /**
    * Returns all hosted email domains.
    * @return a Collection of domain names
    * @throws DAOException if a JDBC error occurs
    */
   public Collection<String> getDomains() throws DAOException {
	   try {
		   prepareStatement("SELECT domain FROM postfix.domain WHERE (active=?)");
		   _ps.setBoolean(1, true);
		   Collection<String> results = new ArrayList<String>();
		   try (ResultSet rs = _ps.executeQuery()) {
			   while (rs.next())
			   	results.add(rs.getString(1));
		   }
		   
		   _ps.close();
		   return results;
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }
   
   /*
    * Helper method to load EMail information.
    */
   private List<IMAPConfiguration> execute() throws SQLException {
      Map<String, IMAPConfiguration> results = new HashMap<String, IMAPConfiguration>();
      try (ResultSet rs = _ps.executeQuery()) {
    	  while (rs.next()) {
    		  IMAPConfiguration result = new IMAPConfiguration(rs.getInt(1), rs.getString(2));
    		  result.setMailDirectory(rs.getString(3));
    		  result.setQuota(rs.getInt(4));
    		  result.setAllowSMTP(rs.getBoolean(5));
    		  result.setActive(rs.getBoolean(6));
    		  results.put(result.getAddress(), result);
    	  }
      }
      
      _ps.close();

      // If we've retrieved nothing, exit
      if (results.isEmpty())
    	  return Collections.emptyList();
      
      // Fetch aliases
      prepareStatementWithoutLimits("SELECT a.goto, a.address FROM postfix.alias a, postfix.mailbox m WHERE (a.goto=m.username) AND (m.ID > 0)");
      try (ResultSet rs = _ps.executeQuery()) {
    	  while (rs.next()) {
    		  IMAPConfiguration cfg = results.get(rs.getString(1));
    		  if (cfg != null)
    			  cfg.addAlias(rs.getString(2));
    	  }
      }
      
      _ps.close();
      return new ArrayList<IMAPConfiguration>(results.values());
   }
}