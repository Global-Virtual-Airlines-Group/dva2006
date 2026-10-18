// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.AISummary;

/**
 * A Data Access Object to load AI-generated summaries from the database. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class GetAISummary extends DAO {

	/**
	 * Creates the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAISummary(Connection c) {
		super(c);
	}

	/**
	 * Loads a yearly review summary from the database.
	 * @param pilotID the Pilot databsae ID
	 * @param year the year
	 * @return an AISummary, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public AISummary getYearlyReview(int pilotID, int year) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM YEARLY_SUMMARY WHERE (ID=?) AND (YEAR=?) LIMIT 1")) {
			ps.setInt(1, pilotID);
			ps.setInt(2, year);
			
			// Execute the query
			AISummary s = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					s = new AISummary(rs.getInt(1), rs.getString(7));
					s.setCreatedOn(toInstant(rs.getTimestamp(3)));
					s.setTokens(rs.getInt(4), rs.getInt(5));
					s.setGenerationTime(rs.getLong(6));
				}
			}
			
			return s;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}