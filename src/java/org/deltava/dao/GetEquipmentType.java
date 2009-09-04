// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.CollectionUtils;
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
	 * Returns a particular Equipment Program profile. If multiple airlines have the same equipment program name,
	 * then the results are undefined.
	 * @param eqType the Equipment Type to return
	 * @return the Equipment Type profile or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public EquipmentType get(String eqType) throws DAOException {
		return get(eqType, null);
	}
	
	/**
	 * Returns a particular Equipment Program profile from a specific database.
	 * @param eqType the Equipment Type to return
	 * @param dbName the database name
	 * @return the Equipment Type profile or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public EquipmentType get(String eqType, String dbName) throws DAOException {
		try {
			if (dbName == null) {
				prepareStatementWithoutLimits("SELECT AI.DBNAME, CASE AI.DBNAME WHEN ? THEN 1 ELSE 0 END AS SRT FROM "
						+ "common.AIRLINEINFO AI, common.EQPROGRAMS EP WHERE (EP.AIRLINE=AI.CODE) AND (EP.EQTYPE=?) "
						+ "ORDER BY SRT DESC LIMIT 1");
				_ps.setString(1, SystemData.get("airline.db"));
				_ps.setString(2, eqType);
			
				// Execute the query
				ResultSet rs = _ps.executeQuery();
				dbName = rs.next() ? rs.getString(1) : null;
				rs.close();
				_ps.close();
				if (dbName == null)
					return null;
			}
			
			// Build the SQL statement
			StringBuilder sqlBuf = new StringBuilder("SELECT EQ.*, EP.AIRLINE, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), "
					+ "P.EMAIL FROM ");
			sqlBuf.append(dbName);
			sqlBuf.append(".EQTYPES EQ, common.EQPROGRAMS EP, common.AIRLINEINFO AI, ");
			sqlBuf.append(dbName);
			sqlBuf.append(".PILOTS P WHERE (EP.EQTYPE=EQ.EQTYPE) AND (EQ.CP_ID=P.ID) AND (EP.AIRLINE=AI.CODE) AND "
					+ "(AI.DBNAME=?) AND (EQ.EQTYPE=?) LIMIT 1");

			// Prepare the statement
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, dbName);
			_ps.setString(2, eqType);

			// Execute the query - if we get nothing back, then return null
			List<EquipmentType> results = execute();
			if (results.isEmpty())
				return null;

			// Get the ratings/exams
			loadRatings(results, dbName);
			loadExams(results, dbName);
			loadAirlines(results, dbName);
			return results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all the Equipment Types in a particular stage.
	 * @param stage the stage number
	 * @param dbName the database name
	 * @return a List of EquipmentTypes
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<EquipmentType> getByStage(int stage, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT EQ.*, EP.AIRLINE, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), "
				+ "P.EMAIL FROM common.EQPROGRAMS EP, common.AIRLINEINFO AI, ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".EQTYPES EQ, ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS P WHERE (EP.AIRLINE=AI.CODE) AND (AI.DBNAME=LOWER(?)) AND (EP.EQTYPE=EQ.EQTYPE) "
				+ "AND (EQ.CP_ID=P.ID) AND (EQ.STAGE=?) ORDER BY EQ.STAGE, EQ.EQTYPE");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, dbName);
			_ps.setInt(2, stage);

			// Return results
			List<EquipmentType> results = execute();
			loadRatings(results, dbName);
			loadExams(results, dbName);
			loadAirlines(results, dbName);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all equipment programs a Pilot in a particular airline can get ratings in.
	 * @param aCode the Airline code
	 * @return a Collection of EquipmentType beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<EquipmentType> getAvailable(String aCode) throws DAOException {
		try {
			// Get the programs to load
			prepareStatementWithoutLimits("SELECT DISTINCT AI.DBNAME FROM common.AIRLINEINFO AI, "
					+ "common.EQPROGRAMS EP WHERE (AI.CODE=EP.AIRLINE) AND (LOCATE(?, EP.AIRLINES) > 0)");
			_ps.setString(1, aCode);
			
			// Execute the query
			Collection<String> dbs = new LinkedHashSet<String>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				dbs.add(rs.getString(1));
			
			// Clean up
			rs.close();
			_ps.close();
			
			// Loop through the databases and load the programs
			Collection<EquipmentType> results = new ArrayList<EquipmentType>();
			for (Iterator<String> i = dbs.iterator(); i.hasNext(); ) {
				String db = formatDBName(i.next());
				
				// Build the SQL statement
				StringBuilder buf = new StringBuilder("SELECT DISTINCT EQ.*, EP.AIRLINE, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), "
						+ "P.EMAIL FROM ");
				buf.append(db);
				buf.append(".EQTYPES EQ, common.EQPROGRAMS EP, common.AIRLINEINFO AI, ");
				buf.append(db);
				buf.append(".PILOTS P WHERE (EP.EQTYPE=EQ.EQTYPE) AND (EQ.CP_ID=P.ID) AND (EQ.ACTIVE=?) AND "
						+ "(EP.AIRLINE=AI.CODE) AND (AI.DBNAME=LOWER(?)) AND (EP.EQTYPE=EQ.EQTYPE) AND (LOCATE(?, EP.AIRLINES) > 0)");
				
				// Prepare the statement and execute
				prepareStatementWithoutLimits(buf.toString());
				_ps.setBoolean(1, true);
				_ps.setString(2, db.toLowerCase());
				_ps.setString(3, aCode);
				Collection<EquipmentType> exams = execute();
				
				// Load child rows and add
				loadRatings(exams, db);
				loadExams(exams, db);
				loadAirlines(exams, db);
				results.addAll(new TreeSet<EquipmentType>(exams));
			}
			
			return results;
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
		StringBuilder sqlBuf = new StringBuilder("SELECT EQ.*, EP.AIRLINE, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PILOTS P, common.EQPROGRAMS EP, common.AIRLINEINFO AI, ");
		sqlBuf.append(dbName);
		sqlBuf.append(".EQTYPES EQ WHERE (AI.DBNAME=?) AND (EP.AIRLINE=AI.CODE) AND (EP.EQTYPE=EQ.EQTYPE) AND "
				+ "(EQ.CP_ID=P.ID) AND (EQ.ACTIVE=?) ORDER BY EQ.STAGE, EQ.EQTYPE");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, dbName);
			_ps.setBoolean(2, true);
			List<EquipmentType> results = execute();
			loadRatings(results, dbName);
			loadExams(results, dbName);
			loadAirlines(results, dbName);
			return results;
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
			prepareStatement("SELECT EQ.*, EP.AIRLINE, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL FROM EQTYPES EQ, "
					+ "common.EQPROGRAMS EP, PILOTS P WHERE (EQ.EQTYPE=EP.EQTYPE) AND (EQ.CP_ID=P.ID) AND (EP.AIRLINE=?) "
					+ "ORDER BY EQ.STAGE, EQ.EQTYPE");
			_ps.setString(1, SystemData.get("airline.code"));
			List<EquipmentType> results = execute();
			loadRatings(results, SystemData.get("airline.db"));
			loadExams(results, SystemData.get("airline.db"));
			return results;
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
		sqlBuf.append(".EQRATINGS WHERE (RATING_TYPE=?) AND (RATED_EQ=?) ORDER BY EQTYPE");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, EquipmentType.PRIMARY_RATING);
			_ps.setString(2, eqType);

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			Collection<String> results = new LinkedHashSet<String>();
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
			while (rs.next())
				results.put(rs.getString(1), new Integer(rs.getInt(2)));

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the database IDs for all Pilots missing an assigned rating in a particular Equipment type program.
	 * @param eq the EquipmentType bean
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getPilotsWithMissingRatings(EquipmentType eq) throws DAOException {

		// Build the SQL statement
		Collection<String> allRatings = new HashSet<String>(eq.getPrimaryRatings());
		allRatings.addAll(eq.getSecondaryRatings());
		StringBuilder sqlBuf = new StringBuilder("SELECT P.ID, COUNT(R.RATING) AS CNT FROM PILOTS P LEFT JOIN "
				+ "RATINGS R ON (P.ID=R.ID) WHERE (P.EQTYPE=?) AND R.RATING IN (");
		for (Iterator<String> i = allRatings.iterator(); i.hasNext();) {
			i.next();
			sqlBuf.append('?');
			if (i.hasNext())
				sqlBuf.append(',');
		}

		sqlBuf.append(") GROUP BY P.ID HAVING (CNT < ?)");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, eq.getName());

			int x = 1;
			for (Iterator<String> i = allRatings.iterator(); i.hasNext();) {
				String rating = i.next();
				_ps.setString(++x, rating);
			}

			_ps.setInt(++x, allRatings.size());

			// Execute the query
			Collection<Integer> results = new LinkedHashSet<Integer>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(new Integer(rs.getInt(1)));

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to load type ratings for each equipment type program.
	 */
	private void loadRatings(Collection<EquipmentType> eTypes, String db) throws SQLException {
		Map<String, EquipmentType> types = CollectionUtils.createMap(eTypes, "name");

		// Execute the query
		prepareStatementWithoutLimits("SELECT * FROM " + db + ".EQRATINGS");
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			EquipmentType eq = types.get(rs.getString(1));
			if (eq != null) {
				switch (rs.getInt(2)) {
				case EquipmentType.PRIMARY_RATING:
					eq.addPrimaryRating(rs.getString(3));
					break;

				default:
				case EquipmentType.SECONDARY_RATING:
					eq.addSecondaryRating(rs.getString(3));
					break;
				}
			}
		}
		
		// Clean up
		rs.close();
		_ps.close();
	}

	/**
	 * Helper method to load examinations for each equipment type program.
	 */
	private void loadExams(Collection<EquipmentType> eTypes, String db) throws SQLException {
		Map<String, EquipmentType> types = CollectionUtils.createMap(eTypes, "name");
		
		// Execute the query
		prepareStatementWithoutLimits("SELECT EQTYPE, EXAMTYPE, EXAM FROM " + db + ".EQEXAMS");
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			EquipmentType eq = types.get(rs.getString(1));
			if (eq != null) {
				switch (rs.getInt(2)) {
					default:
					case EquipmentType.EXAM_FO:
						eq.addExam(Ranks.RANK_FO, rs.getString(3));
						break;
						
					case EquipmentType.EXAM_CAPT:
						eq.addExam(Ranks.RANK_C, rs.getString(3));
						break;
				}
			}
		}
		
		// Clean up
		rs.close();
		_ps.close();
	}
	
	/**
	 * Helper method to load airlines for equipment type program.
	 */
	private void loadAirlines(Collection<EquipmentType> eTypes, String db) throws SQLException {
		Map<String, EquipmentType> types = CollectionUtils.createMap(eTypes, "name");
		
		// Execute the query
		prepareStatementWithoutLimits("SELECT EQTYPE, AIRLINE FROM " + db + ".EQAIRLINES");
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			EquipmentType eq = types.get(rs.getString(1));
			if (eq != null)
				eq.addAirline(SystemData.getApp(rs.getString(2)));
		}
		
		// Clean up
		rs.close();
		_ps.close();
	}

	/**
	 * Helper method to iterate through the result set.
	 */
	private List<EquipmentType> execute() throws SQLException {
		List<EquipmentType> results = new ArrayList<EquipmentType>();

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			EquipmentType eq = new EquipmentType(rs.getString(1));
			eq.setCPID(rs.getInt(2));
			eq.setStage(rs.getInt(3));
			eq.addRanks(rs.getString(4), ",");
			eq.setActive(rs.getBoolean(5));
			eq.setPromotionLegs(Ranks.RANK_SO, rs.getInt(6));
			eq.setPromotionHours(Ranks.RANK_SO, rs.getInt(7));
			eq.setPromotionLegs(Ranks.RANK_C, rs.getInt(8));
			eq.setPromotionHours(Ranks.RANK_C, rs.getInt(9));
			eq.setACARSPromotionLegs(rs.getBoolean(10));
			eq.setOwner(SystemData.getApp(rs.getString(11)));
			eq.setCPName(rs.getString(12));
			eq.setCPEmail(rs.getString(13));

			// Add to the results
			results.add(eq);
		}

		// Clean up JDBC resources
		rs.close();
		_ps.close();
		return results;
	}
}