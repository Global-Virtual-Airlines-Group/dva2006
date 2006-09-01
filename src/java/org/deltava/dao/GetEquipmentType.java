// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve equipment type profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetEquipmentType extends DAO {

	/**
	 * Initializes the DAO with a given JDBC connection.
	 * @param c the JDBC Connection
	 */
	public GetEquipmentType(Connection c) {
		super(c);
	}

	/**
	 * Returns a particular Equipment Program profile.
	 * @param eqType the Equipment Type to return
	 * @return the Equipment Type profile
	 * @throws DAOException if a JDBC error occurs
	 */
	public EquipmentType get(String eqType) throws DAOException {
		try {
			prepareStatement("SELECT EQ.*, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL, R.RATING_TYPE, "
					+ "R.RATED_EQ FROM EQTYPES EQ, PILOTS P, EQRATINGS R WHERE (EQ.CP_ID=P.ID) AND "
					+ "(EQ.EQTYPE=R.EQTYPE) AND (EQ.EQTYPE=?) ORDER BY EQ.STAGE, EQ.EQTYPE");
			_ps.setString(1, eqType);

			// Execute the query - if we get nothing back, then return null
			List results = execute();
			return (results.size() == 0) ? null : (EquipmentType) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all the Equipment Types in a particular stage.
	 * @param stage the stage number
	 * @return a List of EquipmentTypes
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<EquipmentType> getByStage(int stage) throws DAOException {
		try {
			prepareStatement("SELECT EQ.*, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL, R.RATING_TYPE, "
					+ "R.RATED_EQ FROM EQTYPES EQ, PILOTS P, EQRATINGS R WHERE (EQ.CP_ID=P.ID) AND "
					+ "(EQ.EQTYPE=R.EQTYPE) AND (EQ.STAGE=?) ORDER BY EQ.STAGE, EQ.EQTYPE");
			_ps.setInt(1, stage);

			// Return results
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all active Equipment Programs.
	 * @param dbName the database name
	 * @return a List of EquipmentTypes
	 * @throws DAOException if a JDBC error occurs
	 * @see GetEquipmentType#getActive()
	 */
	public Collection<EquipmentType> getActive(String dbName) throws DAOException {

		// Build the SQL statement
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT EQ.*, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), "
				+ "P.EMAIL, R.RATING_TYPE, R.RATED_EQ FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PILOTS P, ");
		sqlBuf.append(dbName);
		sqlBuf.append(".EQTYPES EQ LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".EQRATINGS R ON (EQ.EQTYPE=R.EQTYPE) WHERE (EQ.CP_ID=P.ID) AND (EQ.ACTIVE=?) "
				+ "ORDER BY EQ.STAGE, EQ.EQTYPE");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setBoolean(1, true);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all active Equipment Programs in the current airline.
	 * @return a List of EquipmentTypes
	 * @throws DAOException if a JDBC error occurs
	 * @see GetEquipmentType#getActive(String)
	 */
	public Collection<EquipmentType> getActive() throws DAOException {
		return getActive(SystemData.get("airline.db"));
	}

	/**
	 * Returns all Equipment Types.
	 * @return a List of EquipmentTypes
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<EquipmentType> getAll() throws DAOException {
		try {
			prepareStatement("SELECT EQ.*, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL, R.RATING_TYPE, "
					+ "R.RATED_EQ FROM EQTYPES EQ, PILOTS P, EQRATINGS R WHERE (EQ.CP_ID=P.ID) AND "
					+ "(EQ.EQTYPE=R.EQTYPE) ORDER BY EQ.STAGE, EQ.EQTYPE");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the Equipment Programs for whom a flight in a given aircraft counts for promotion.
	 * @param dbName the Database name
	 * @param eqType the Aircraft type
	 * @return a Collection of equipment program names
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<String> getPrimaryTypes(String dbName, String eqType) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT EQTYPE FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".EQRATINGS WHERE (RATING_TYPE=?) AND (RATED_EQ=?)");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, EquipmentType.PRIMARY_RATING);
			_ps.setString(2, eqType);

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			Set<String> results = new HashSet<String>();
			while (rs.next())
				results.add(rs.getString(1));

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the number of Active Pilots in each Equipment Program.
	 * @return a Map of pilot numbers, keyed by Equipment Type name
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, Integer> getPilotCounts() throws DAOException {
		try {
			prepareStatement("SELECT P.EQTYPE, COUNT(P.ID) FROM PILOTS P, EQTYPES EQ WHERE (P.STATUS=?) "
					+ "AND (P.EQTYPE=EQ.EQTYPE) GROUP BY P.EQTYPE ORDER BY EQ.STAGE DESC, P.EQTYPE");
			_ps.setInt(1, Pilot.ACTIVE);

			// Execute the query
			Map<String, Integer> results = new LinkedHashMap<String, Integer>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				results.put(rs.getString(1), new Integer(rs.getInt(2)));
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to iterate through the result set.
	 */
	private List<EquipmentType> execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List<EquipmentType> results = new ArrayList<EquipmentType>();
		EquipmentType eq = new EquipmentType("");
		while (rs.next()) {
			String eqName = rs.getString(1);

			// If it's a new equipment type, then add it to the list and populate it
			if (!eqName.equals(eq.getName())) {
				eq = new EquipmentType(eqName);
				eq.setCPID(rs.getInt(2));
				eq.setStage(rs.getInt(3));
				eq.addRanks(rs.getString(4), ",");
				eq.setExamName(Ranks.RANK_FO, rs.getString(5));
				eq.setExamName(Ranks.RANK_C, rs.getString(6));
				eq.setActive(rs.getBoolean(7));
				eq.setPromotionLegs(Ranks.RANK_SO, rs.getInt(8));
				eq.setPromotionHours(Ranks.RANK_SO, rs.getInt(9));
				eq.setPromotionLegs(Ranks.RANK_C, rs.getInt(10));
				eq.setPromotionHours(Ranks.RANK_C, rs.getInt(11));
				eq.setACARSPromotionLegs(rs.getBoolean(12));
				eq.setCPName(rs.getString(13));
				eq.setCPEmail(rs.getString(14));

				// Add to the results
				results.add(eq);
			}

			// The last two columns are the additional rating info
			switch (rs.getInt(14)) {
				case EquipmentType.PRIMARY_RATING:
					eq.addPrimaryRating(rs.getString(15));
					break;

				default:
				case EquipmentType.SECONDARY_RATING:
					eq.addSecondaryRating(rs.getString(15));
					break;
			}
		}

		// Clean up JDBC resources
		rs.close();
		_ps.close();
		return results;
	}
}