// Copyright 2006, 2009, 2010, 2019, 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.fleet.Resource;

/**
 * A Data Access Object to write Web Resources to a database.
 * @author Luke
 * @version 10.4
 * @since 1.0
 */

public class SetResource extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetResource(Connection c) {
		super(c);
	}

	/**
	 * Writes a Web Resource to the database.
	 * @param r the Resource bean
	 * @throws DAOException if a JDBC errror occurs
	 */
	public void write(Resource r) throws DAOException {
		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO RESOURCES (ID, URL, TITLE, DOMAIN, REMARKS, CATEGORY, CREATEDON, AUTHOR, UPDATEDBY, HITCOUNT, IGNORE_CERTS, ISPUBLIC) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, r.getID());
				ps.setString(2, r.getURL());
				ps.setString(3, r.getTitle());
				ps.setString(4, r.getDomain());
				ps.setString(5, r.getDescription());
				ps.setString(6, r.getCategory());
				ps.setTimestamp(7, createTimestamp(r.getCreatedOn()));
				ps.setInt(8, r.getAuthorID());
				ps.setInt(9, r.getLastUpdateID());
				ps.setInt(10, r.getHits());
				ps.setBoolean(11, r.getPublic());
				ps.setBoolean(12, r.getIgnoreCertifications());
				executeUpdate(ps, 1);
			}
			
			// Clean out the certification names, els get new ID
			if (r.getID() != 0) {
				try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.CERTRSRCS WHERE (ID=?)")) {
					ps.setInt(1, r.getID());
					executeUpdate(ps, 0);
				}
			} else
				r.setID(getNewID());
			
			// Write the certification names
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.CERTRSRCS (ID, CERT) VALUES (?, ?)")) {
				ps.setInt(1, r.getID());
				for (String cert : r.getCertifications()) {
					ps.setString(2, cert);
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, r.getCertifications().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Logs a link to a particular Web Resource.
	 * @param id the Resource database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void hit(int id) throws DAOException {
		try (PreparedStatement ps = prepare("UPDATE RESOURCES SET HITCOUNT=HITCOUNT+1 WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Web Resource from the database.
	 * @param id the Resource database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM RESOURCES WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}