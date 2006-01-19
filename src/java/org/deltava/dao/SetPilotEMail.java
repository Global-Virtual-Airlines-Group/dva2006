// Copyright (c) 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.EMailConfiguration;

/**
 * A Data Access Object to update Pilot IMAP data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetPilotEMail extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetPilotEMail(Connection c) {
		super(c);
	}
	
	/**
	 * Deletes a user's e-mail configuration. <i>This will not delete the mailbox on the server, merely the address
	 * records used by Postfix and Courier.</i>
	 * @param id the Pilot's database ID 
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM postfix.mailbox WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a user's e-mail configuration to the database.
	 * @param cfg the EMailConfiguration bean
	 * @param name the Pilot name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(EMailConfiguration cfg, String name) throws DAOException {
		try {
			startTransaction();
            
            // Clean out the aliases
            prepareStatementWithoutLimits("DELETE FROM postfix.alias WHERE (goto=?)");
            _ps.setString(1, cfg.getAddress());
            executeUpdate(0);
			
			// Write the mailbox record
			prepareStatement("REPLACE INTO postfix.mailbox (username, name, maildir, quota, active, ID) VALUES "
					+ "(?, ?, ?, ?, ?, ?)");
			_ps.setString(1, cfg.getAddress());
			_ps.setString(2, name);
			_ps.setString(3, cfg.getMailDirectory());
			_ps.setInt(4, cfg.getQuota());
			_ps.setBoolean(5, cfg.getActive());
			_ps.setInt(6, cfg.getID());
			executeUpdate(1);
			
			// Write the aliases
			prepareStatement("INSERT INTO postfix.alias (address, goto, active) VALUES (?, ?, ?)");
			_ps.setString(2, cfg.getAddress());
			_ps.setBoolean(3, true);
			for (Iterator i = cfg.getAliases().iterator(); i.hasNext(); ) {
				String alias = (String) i.next();
				_ps.setString(1, alias);
				_ps.addBatch();
			}
			
			// Update the table
			_ps.executeBatch();
			_ps.close();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}		
	}
	
	/**
	 * Updates a Pilot's IMAP mailbox password.
	 * @param id the Pilot's database ID
	 * @param pwd the new password
	 */
	public void updatePassword(int id, String pwd) throws DAOException {
		try {
			prepareStatement("UPDATE postfix.mailbox SET crypt_pw=ENCRYPT(?) WHERE (ID=?)");
			_ps.setString(1, pwd);
			_ps.setInt(2, id);
			
			// Execute the query
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}