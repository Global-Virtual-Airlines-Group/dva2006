// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.PartnerInfo;

/**
 * A Data Access Object to write virtual airline Partner information to the database. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class SetPartner extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetPartner(Connection c) {
		super(c);
	}

	/**
	 * Updates a Partner record in the database. 
	 * @param pi the PartnerInfo bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(PartnerInfo pi) throws DAOException {
		boolean isNew = (pi.getID() == 0);
		try {
			startTransaction();
			try (PreparedStatement ps = prepare("INSERT INTO PARTNERS (ID, NAME, URL, DESCRIPTION, REFERCOUNT) VALUES (?, ?, ?, ?, ?) AS N ON DUPLICATE KEY UPDATE NAME=N.NAME, URL=N.URL, DESCRIPTION=N.DESCRIPTION, REFERCOUNT=N.REFERCOUNT")) {
				ps.setInt(1, pi.getID());
				ps.setString(2, pi.getName());
				ps.setString(3, pi.getURL());
				ps.setString(4, pi.getDescription());
				ps.setInt(5, pi.getReferCount());
				executeUpdate(ps, 1);
			}
			
			// Get the ID
			if (isNew)
				pi.setID(getNewID());
			
			// Save image
			if (pi.isLoaded()) {
				try (PreparedStatement ps = prepare("REPLACE INTO PARTNER_IMGS (ID, IMG, X, Y, EXT) VALUES (?, ?, ?, ?, ?)")) {
					ps.setInt(1, pi.getID());
					ps.setBlob(2, pi.getInputStream());
					ps.setInt(3, pi.getWidth());
					ps.setInt(4, pi.getHeight());
					ps.setString(5, pi.getTypeName());
					executeUpdate(ps, 1);
				}
			} else if (!isNew) {
				try (PreparedStatement ps = prepare("DELETE FROM PARTNER_IMGS WHERE (ID=?)")) {
					ps.setInt(1, pi.getID());
					executeUpdate(ps, 0);
				}
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Increments the refer count for a particular Partner. 
	 * @param id the Partner database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void refer(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE PARTNERS SET REFERCOUNT=REFERCOUNT+1 WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Partner from the database.
	 * @param id the Partner database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM PARTNERS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}