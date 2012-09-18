// Copyright 2005, 2007, 2008, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.io.IOException;

import org.deltava.beans.system.IMAPConfiguration;

import org.deltava.util.ThreadUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Pilot IMAP mailbox information.
 * @author Luke
 * @version 5.0
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
           prepareStatementWithoutLimits("SELECT ID, username, maildir, quota, active FROM postfix.mailbox "
        		   + "WHERE (ID=?) LIMIT 1");
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
         prepareStatement("SELECT ID, username, maildir, quota, active FROM postfix.mailbox WHERE (ID>0) ORDER BY ID");
         return execute();
      } catch (SQLException se) {
         throw new DAOException(se); 
      }
   }
   
   /**
    * Checks whether a particular mailbox has new mail in it. This call executes a script that checks a maildir
    * @param path the user's mail database path
    * @return the number of messages waiting
    * @throws DAOException if an error occurs
    */
   public int hasNewMail(String path) throws DAOException {
	   try {
		   ProcessBuilder pBuilder = new ProcessBuilder(SystemData.get("smtp.imap.newmail"), path);
		   pBuilder.redirectErrorStream(true);
		   Process p = pBuilder.start();
		   
			// Wait for the process to complete
			int runTime = 0;
			while (runTime < 1250) {
				ThreadUtils.sleep(100);
				try {
					return p.exitValue();
				} catch (IllegalThreadStateException itse) {
					runTime += 100;            			
				}
			}
			
			return 0;
	   } catch (IOException ie) {
		   throw new DAOException(ie);
	   }
   }
   
   /**
    * Helper method to load EMail information.
    */
   private List<IMAPConfiguration> execute() throws SQLException {
      Map<String, IMAPConfiguration> results = new HashMap<String, IMAPConfiguration>();
      try (ResultSet rs = _ps.executeQuery()) {
    	  while (rs.next()) {
    		  IMAPConfiguration result = new IMAPConfiguration(rs.getInt(1), rs.getString(2));
    		  result.setMailDirectory(rs.getString(3));
    		  result.setQuota(rs.getInt(4));
    		  result.setActive(rs.getBoolean(5));
    		  results.put(result.getAddress(), result);
    	  }
      }
      
      _ps.close();

      // If we've retrieved nothing, exit
      if (results.isEmpty())
    	  return Collections.emptyList();

      // Build SQL statement
      StringBuilder sqlBuf = new StringBuilder("SELECT goto, address FROM postfix.alias WHERE (goto IN (");
      for (Iterator<String> i = results.keySet().iterator(); i.hasNext(); ) {
         String addr = i.next();
         sqlBuf.append('\'');
         sqlBuf.append(addr);
         sqlBuf.append(i.hasNext() ? "\'," : "\'))");
      }
      
      // Fetch aliases
      prepareStatementWithoutLimits(sqlBuf.toString());
      try (ResultSet rs = _ps.executeQuery()) {
    	  while (rs.next()) {
    		  IMAPConfiguration cfg = results.get(rs.getString(1));
    		  if (cfg != null)
    			  cfg.addAlias(rs.getString(2));
    	  }
      }
      
      // Clean up and return
      _ps.close();
      return new ArrayList<IMAPConfiguration>(results.values());
   }
}