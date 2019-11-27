// Copyright 2005, 2006, 2012, 2015, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.commons.codec.digest.UnixCrypt;

import org.deltava.beans.system.IMAPConfiguration;
import org.deltava.crypt.SaltMine;

/**
 * A Data Access Object to update Pilot IMAP data.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM postfix.mailbox WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE postfix.mailbox SET active=?, allow_smtp=? WHERE (ID=?)")) {
			ps.setBoolean(1, false);
			ps.setBoolean(2, false);
			ps.setInt(3, id);
			executeUpdate(ps, 0);
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
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO postfix.mailbox (username, name, maildir, quota, allow_smtp, active, ID) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, cfg.getAddress());
				ps.setString(2, name);
				ps.setString(3, cfg.getMailDirectory());
				ps.setInt(4, cfg.getQuota());
				ps.setBoolean(5, cfg.getAllowSMTP());
				ps.setBoolean(6, cfg.getActive());
				ps.setInt(7, cfg.getID());
				executeUpdate(ps, 1);
			}

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
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM postfix.alias WHERE (goto=?)")) {
				ps.setString(1, cfg.getAddress());
				executeUpdate(ps, 0);
			}
			
			// Update the mailbox record
			try (PreparedStatement ps = prepare("UPDATE postfix.mailbox SET username=?, name=?, maildir=?, quota=?, allow_smtp=?, active=? WHERE (ID=?)")) {
				ps.setString(1, cfg.getAddress());
				ps.setString(2, name);
				ps.setString(3, cfg.getMailDirectory());
				ps.setInt(4, cfg.getQuota());
				ps.setBoolean(5, cfg.getAllowSMTP());
				ps.setBoolean(6, cfg.getActive());
				ps.setInt(7, cfg.getID());
				executeUpdate(ps, 1);
			}
			
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
		String salt = SaltMine.generate(2);
		try (PreparedStatement ps = prepare("UPDATE postfix.mailbox SET crypt_pw=?, sha_pw=SHA1(?) WHERE (ID=?)")) {
			ps.setString(1, UnixCrypt.crypt(pwd, salt));
			ps.setString(2, pwd);
			ps.setInt(3, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to write alias records.
	 */
	private void writeAliases(String addr, Collection<String> aliases) throws SQLException {
		
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO postfix.alias (address, goto, active) VALUES (?, ?, ?)")) {
			ps.setString(2, addr);
			ps.setBoolean(3, true);
			for (String alias : aliases) {
				ps.setString(1, alias);
				ps.addBatch();
			}

			executeUpdate(ps, 1, aliases.size());
		}
	}
}