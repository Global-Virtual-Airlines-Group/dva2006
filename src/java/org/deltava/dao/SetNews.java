// Copyright 2005, 2019, 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.News;
import org.deltava.beans.Notice;

/**
 * A Data Access Object to write System News and NOTAMs to the database.
 * @author Luke
 * @version 10.6
 * @since 1.0
 */

public class SetNews extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetNews(Connection c) {
		super(c);
	}

	/**
	 * Writes a System News entry to the database. This can handle inserts and edits.
	 * @param n the News entry to write
	 * @throws DAOException if a JDBC error occ
	 */
	public void write(News n) throws DAOException {
		boolean isNew = (n.getID() == 0);
		try {
			startTransaction();
			try (PreparedStatement ps = prepare("INSERT INTO NEWS (PILOT_ID, DATE, ISHTML, SUBJECT, BODY, ID) VALUES (?, ?, ?, ?, ?, ?) AS N ON DUPLICATE KEY UPDATE PILOT_ID=N.PILOT_ID, DATE=N.DATE, ISHTML=N.ISHTML, SUBJECT=N.SUBJECT, BODY=N.BODY")) {
				ps.setInt(1, n.getAuthorID());
				ps.setTimestamp(2, createTimestamp(n.getDate()));
				ps.setBoolean(3, n.getIsHTML());
				ps.setString(4, n.getSubject());
				ps.setString(5, n.getBody());
				ps.setInt(6, n.getID());
				executeUpdate(ps, 1);
			}

			// Get the ID
			if (isNew)
				n.setID(getNewID());

			// Write the header image
			if (n.isLoaded()) {
				try (PreparedStatement ps = prepare("REPLACE INTO NEWS_IMGS (ID, IMG, X, Y, WIDTH, TYPE) VALUES (?, ?, ?, ?, ?, ?)")) {
					ps.setInt(1, n.getID());
					ps.setBlob(2, n.getInputStream());
					ps.setInt(3, n.getWidth());
					ps.setInt(4, n.getHeight());
					ps.setInt(5, n.getBannerWidth());
					ps.setInt(6, n.getFormat().ordinal());
					executeUpdate(ps, 1);
				}
			} else if (!isNew) {
				try (PreparedStatement ps = prepare("DELETE FROM NEWS_IMGS WHERE (ID=?)")) {
					ps.setInt(1, n.getID());
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
	 * Writes a Notice to Airmen (NOTAM) to the database. This can handle inserts and edits.
	 * @param n the Notice entry to write
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Notice n) throws DAOException {
		boolean isNew = (n.getID() == 0);
		try {
			startTransaction();
			try (PreparedStatement ps = prepare("INSERT INTO NOTAMS (PILOT_ID, EFFDATE, SUBJECT, BODY, ACTIVE, ISHTML, ID)  VALUES (?, ?, ?, ?, ?, ?, ?) AS N ON DUPLICATE KEY UPDATE PILOT_ID=N.PILOT_ID, EFFDATE=N.EFFDATE, SUBJECT=N.SUBJECT, "
				+ "BODY=N.BODY, ACTIVE=N.ACTIVE, ISHTML=N.ISHTML")) {
				ps.setInt(1, n.getAuthorID());
				ps.setTimestamp(2, createTimestamp(n.getDate()));
				ps.setString(3, n.getSubject());
				ps.setString(4, n.getBody());
				ps.setBoolean(5, n.getActive());
				ps.setBoolean(6, n.getIsHTML());
				ps.setInt(7, n.getID());
				executeUpdate(ps, 1);
			}

			// Get the ID
			if (isNew)
				n.setID(getNewID());

			// Write the header image
			if (n.isLoaded()) {
				try (PreparedStatement ps = prepare("REPLACE INTO NOTAM_IMGS (ID, IMG, X, Y, WIDTH, TYPE) VALUES (?, ?, ?, ?, ?, ?)")) {
					ps.setInt(1, n.getID());
					ps.setBlob(2, n.getInputStream());
					ps.setInt(3, n.getWidth());
					ps.setInt(4, n.getHeight());
					ps.setInt(5, n.getBannerWidth());
					ps.setInt(6, n.getFormat().ordinal());
					executeUpdate(ps, 1);
				}
			} else if (!isNew) {
				try (PreparedStatement ps = prepare("DELETE FROM NOTAM_IMGS WHERE (ID=?)")) {
					ps.setInt(1, n.getID());
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
	 * Deletes a News Entry/NOTAM from the database.
	 * @param id the Database ID of the NOTAM or News Entry
	 * @param isNOTAM TRUE if deleting a NOTAM, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id, boolean isNOTAM) throws DAOException {

		// Build SQL statement
		StringBuilder sqlBuf = new StringBuilder("DELETE FROM ");
		sqlBuf.append(isNOTAM ? "NOTAMS" : "NEWS");
		sqlBuf.append(" WHERE (ID=?)");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}