// Copyright 2005, 2006, 2012, 2015, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.commons.codec.digest.Crypt;

import org.deltava.beans.system.IMAPConfiguration;

/**
 * A Data Access Object to update Pilot IMAP data.
 * @author Luke
 * @version 8.3
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
	 * records used by Postfix and Dovecot.</i>
	 * @param id the Pilot's database ID 
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM postfix.mailbox WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Disables a user's e-mail address.
	 * @param id the Pilot's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void disable(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE postfix.mailbox SET active=?, allow_smtp=? WHERE (ID=?)");
			_ps.setBoolean(1, false);
			_ps.setBoolean(2, false);
			_ps.setInt(3, id);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a new e-mail configuration to the database.
	 * @param cfg the EMailConfiguration bean
	 * @param name the Pilot name 
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(IMAPConfiguration cfg, String name) throws DAOException {
		try {
			startTransaction();
			
			// Write the record
        	prepareStatement("INSERT INTO postfix.mailbox (username, name, maildir, quota, allow_smtp, active, ID) VALUES (?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, cfg.getAddress());
			_ps.setString(2, name);
			_ps.setString(3, cfg.getMailDirectory());
			_ps.setInt(4, cfg.getQuota());
			_ps.setBoolean(5, cfg.getAllowSMTP());
			_ps.setBoolean(6, cfg.getActive());
        	_ps.setInt(7, cfg.getID());
			executeUpdate(1);

			// Write the aliases and commit
			writeAliases(cfg.getAddress(), cfg.getAliases());
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates a user's e-mail configuration to the database.
	 * @param cfg the EMailConfiguration bean
	 * @param name the Pilot name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(IMAPConfiguration cfg, String name) throws DAOException {
		try {
			startTransaction();
			
            // Clean out the aliases
            prepareStatementWithoutLimits("DELETE FROM postfix.alias WHERE (goto=?)");
            _ps.setString(1, cfg.getAddress());
            executeUpdate(0);
			
			// Update the mailbox record
            prepareStatement("UPDATE postfix.mailbox SET username=?, name=?, maildir=?, quota=?, allow_smtp=?, active=? WHERE (ID=?)");
			_ps.setString(1, cfg.getAddress());
			_ps.setString(2, name);
			_ps.setString(3, cfg.getMailDirectory());
			_ps.setInt(4, cfg.getQuota());
			_ps.setBoolean(5, cfg.getAllowSMTP());
			_ps.setBoolean(6, cfg.getActive());
        	_ps.setInt(7, cfg.getID());
			executeUpdate(1);
			
			// Write the aliases and commit
			writeAliases(cfg.getAddress(), cfg.getAliases());
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}		
	}
	
	/**
	 * Updates a Pilot's IMAP mailbox password.
	 * @param id the Pilot's database ID
	 * @param pwd the new password
	 * @throws DAOException if a JDBC error occurs
	 */
	public void updatePassword(int id, String pwd) throws DAOException {
		try {
			prepareStatement("UPDATE postfix.mailbox SET crypt_pw=?, sha_pw=SHA1(?) WHERE (ID=?)");
			_ps.setString(1, Crypt.crypt(pwd));
			_ps.setString(2, pwd);
			_ps.setInt(3, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to write alias records.
	 */
	private void writeAliases(String addr, Collection<String> aliases) throws SQLException {
		
		prepareStatementWithoutLimits("INSERT INTO postfix.alias (address, goto, active) VALUES (?, ?, ?)");
		_ps.setString(2, addr);
		_ps.setBoolean(3, true);
		for (String alias : aliases) {
			_ps.setString(1, alias);
			_ps.addBatch();
		}

		executeBatchUpdate(1, aliases.size());
	}
}