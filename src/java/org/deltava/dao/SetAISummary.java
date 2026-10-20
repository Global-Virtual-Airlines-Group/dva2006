// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.AISummary;

/**
 * A Data Access Object to write AI summaries to the database. 
 * @author Luke
 * @version 13.0
 * @since 12.5
 */

public class SetAISummary extends DAO {

	/**
	 * Creates the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAISummary(Connection c) {
		super(c);
	}

	/**
	 * Writes a yearly summary for a Pilot. 
	 * @param year the year
	 * @param msg the AISummary
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeSummary(int year, AISummary msg) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO YEARLY_SUMMARY VALUES (?,?,?,?,?,?,?)")) {
			ps.setInt(1, msg.getID());
			ps.setInt(2, year);
			ps.setTimestamp(3, createTimestamp(msg.getCreatedOn()));
			ps.setInt(4, msg.getInputTokens());
			ps.setInt(5, msg.getOutputTokens());
			ps.setLong(6, msg.getGenerationTime());
			ps.setString(7, msg.getText());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}