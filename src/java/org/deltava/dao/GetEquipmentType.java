// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2013, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.cache.*;
import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve equipment type profiles.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GetEquipmentType extends DAO {
	
	private static final Cache<EquipmentType> _cache = CacheManager.get(EquipmentType.class, "EquipmentTypes");

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
			String db = dbName;
			if (dbName == null) {
				prepareStatementWithoutLimits("SELECT AI.DBNAME, CASE AI.DBNAME WHEN ? THEN 1 ELSE 0 END AS SRT FROM "
						+ "common.AIRLINEINFO AI, common.EQPROGRAMS EP WHERE (EP.OWNER=AI.CODE) AND (EP.EQTYPE=?) "
						+ "ORDER BY SRT DESC LIMIT 1");
				_ps.setString(1, SystemData.get("airline.db"));
				_ps.setString(2, eqType);
			
				// Execute the query
				try (ResultSet rs = _ps.executeQuery()) {
					if (rs.next())
						db = rs.getString(1);
				}
				
				_ps.close();
				if (db == null)
					return null;
			}
			
			// Check the cache
			EquipmentType eq = _cache.get(dbName + "!!" + eqType);
			if (eq != null)
				return eq;
			
			// Build the SQL statement
			StringBuilder sqlBuf = new StringBuilder("SELECT EQ.*, EP.OWNER, EP.STAGE, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL FROM ");
			sqlBuf.append(db);
			sqlBuf.append(".EQTYPES EQ, common.EQPROGRAMS EP, common.AIRLINEINFO AI, ");
			sqlBuf.append(db);
			sqlBuf.append(".PILOTS P WHERE (EP.EQTYPE=EQ.EQTYPE) AND (EQ.CP_ID=P.ID) AND (EP.OWNER=AI.CODE) AND (AI.DBNAME=?) AND (EQ.EQTYPE=?) LIMIT 1");

			// Prepare the statement
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, db);
			_ps.setString(2, eqType);

			// Execute the query - if we get nothing back, then return null
			List<EquipmentType> results = execute();
			if (results.isEmpty())
				return null;

			// Get the ratings/exams
			loadRatings(results, db);
			loadExams(results, db);
			loadAirlines(results);
			loadSize(results, db);
			_cache.addAll(results);
			return results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the default equipment type name for an Airline.
	 * @param dbName the airline's database name
	 * @return the EquipmentType
	 * @throws DAOException if a JDBC error occurs
	 */
	public String getDefault(String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT EQTYPE FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".EQTYPES WHERE (ISDEFAULT=?) LIMIT 1");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setBoolean(1, true);
			String eqName = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					eqName = rs.getString(1);
			}
			
			_ps.close();
			return eqName;
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
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT EQ.*, EP.OWNER, EP.STAGE, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL FROM common.EQPROGRAMS EP, common.AIRLINEINFO AI, ");
		sqlBuf.append(db);
		sqlBuf.append(".EQTYPES EQ, ");
		sqlBuf.append(db);
		sqlBuf.append(".PILOTS P WHERE (EP.OWNER=AI.CODE) AND (AI.DBNAME=?) AND (EP.EQTYPE=EQ.EQTYPE) AND (EQ.CP_ID=P.ID) AND (EP.STAGE=?) ORDER BY EP.STAGE, EQ.EQTYPE");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, db);
			_ps.setInt(2, stage);

			// Return results
			List<EquipmentType> results = execute();
			loadRatings(results, db);
			loadExams(results, db);
			loadAirlines(results);
			loadSize(results, db);
			_cache.addAll(results);
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
				+ "common.EQPROGRAMS EP, common.EQAIRLINES EA WHERE (AI.CODE=EP.OWNER) AND "
				+ "(EP.EQTYPE=EA.EQTYPE) AND (EP.OWNER=EA.OWNER) AND (EA.AIRLINE=?)");
			_ps.setString(1, aCode);
			
			// Execute the query
			Collection<String> dbs = new LinkedHashSet<String>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					dbs.add(rs.getString(1));
			}
			
			_ps.close();
			
			// Loop through the databases and load the programs
			Collection<EquipmentType> results = new ArrayList<EquipmentType>();
			for (Iterator<String> i = dbs.iterator(); i.hasNext(); ) {
				String db = formatDBName(i.next());
				
				// Build the SQL statement
				StringBuilder buf = new StringBuilder("SELECT DISTINCT EQ.*, EP.OWNER, EP.STAGE, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL FROM ");
				buf.append(db);
				buf.append(".EQTYPES EQ, common.EQAIRLINES EA, common.EQPROGRAMS EP, common.AIRLINEINFO AI, ");
				buf.append(db);
				buf.append(".PILOTS P WHERE (EP.EQTYPE=EQ.EQTYPE) AND (EQ.CP_ID=P.ID) AND (EQ.ACTIVE=?) AND "
					+ "(EP.OWNER=AI.CODE) AND (AI.DBNAME=?) AND (EP.EQTYPE=EQ.EQTYPE) AND (EP.OWNER=EA.OWNER) AND "
					+ "(EP.EQTYPE=EA.EQTYPE) AND (EA.AIRLINE=?)");
				
				// Prepare the statement and execute
				prepareStatementWithoutLimits(buf.toString());
				_ps.setBoolean(1, true);
				_ps.setString(2, db.toLowerCase());
				_ps.setString(3, aCode);
				Collection<EquipmentType> exams = execute();
				
				// Load child rows and add
				loadRatings(exams, db);
				loadExams(exams, db);
				loadAirlines(exams);
				loadSize(exams, db);
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
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT EQ.*, EP.OWNER, EP.STAGE, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".PILOTS P, common.EQPROGRAMS EP, common.AIRLINEINFO AI, ");
		sqlBuf.append(db);
		sqlBuf.append(".EQTYPES EQ WHERE (AI.DBNAME=?) AND (EP.OWNER=AI.CODE) AND (EP.EQTYPE=EQ.EQTYPE) AND "
			+ "(EQ.CP_ID=P.ID) AND (EQ.ACTIVE=?) ORDER BY EP.STAGE, EQ.EQTYPE");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, db);
			_ps.setBoolean(2, true);
			List<EquipmentType> results = execute();
			loadRatings(results, db);
			loadExams(results, db);
			loadAirlines(results);
			loadSize(results, db);
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
			String db = SystemData.get("airline.db");
			prepareStatement("SELECT EQ.*, EP.OWNER, EP.STAGE, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL FROM EQTYPES EQ, "
					+ "common.EQPROGRAMS EP, PILOTS P WHERE (EQ.EQTYPE=EP.EQTYPE) AND (EQ.CP_ID=P.ID) AND (EP.OWNER=?) "
					+ "ORDER BY EP.STAGE, EQ.EQTYPE");
			_ps.setString(1, SystemData.get("airline.code"));
			List<EquipmentType> results = execute();
			loadRatings(results, db);
			loadExams(results, db);
			loadSize(results, db);
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
			_ps.setInt(1, EquipmentType.Rating.PRIMARY.ordinal());
			_ps.setString(2, eqType);

			// Execute the query
			Collection<String> results = new LinkedHashSet<String>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}

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
		try {
			prepareStatementWithoutLimits("SELECT P.ID, COUNT(R.RATING) AS PCNT, (SELECT COUNT(RATED_EQ) FROM EQRATINGS "
				+ "WHERE (EQTYPE=?)) AS EQCNT FROM PILOTS P LEFT JOIN RATINGS R ON (P.ID=R.ID) AND (R.RATING IN (SELECT RATED_EQ "
				+ "FROM EQRATINGS WHERE (EQTYPE=?))) WHERE (P.EQTYPE=?) GROUP BY P.ID HAVING (PCNT<EQCNT)");
			_ps.setString(1, eq.getName());
			_ps.setString(2, eq.getName());
			_ps.setString(3, eq.getName());

			// Execute the query
			Collection<Integer> results = new LinkedHashSet<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));	
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to load type ratings for each equipment type program.
	 */
	private void loadRatings(Collection<EquipmentType> eTypes, String db) throws SQLException {
		Map<String, EquipmentType> types = CollectionUtils.createMap(eTypes, "name");
		prepareStatementWithoutLimits("SELECT * FROM " + db + ".EQRATINGS");
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				EquipmentType eq = types.get(rs.getString(1));
				if (eq != null) {
					EquipmentType.Rating rt = EquipmentType.Rating.values()[rs.getInt(2)];
					switch (rt) {
					case PRIMARY:
						eq.addPrimaryRating(rs.getString(3));
						break;

					default:
						eq.addSecondaryRating(rs.getString(3));
					}
				}
			}
		}
		
		_ps.close();
	}

	/*
	 * Helper method to load examinations for each equipment type program.
	 */
	private void loadExams(Collection<EquipmentType> eTypes, String db) throws SQLException {
		Map<String, EquipmentType> types = CollectionUtils.createMap(eTypes, "name");
		prepareStatementWithoutLimits("SELECT EQTYPE, EXAMTYPE, EXAM FROM " + db + ".EQEXAMS");
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				EquipmentType eq = types.get(rs.getString(1));
				if (eq != null) {
					Rank rnk = Rank.values()[rs.getInt(2)];
					if (rnk.ordinal() <= Rank.C.ordinal())
						eq.addExam(rnk, rs.getString(3));
				}
			}
		}
		
		_ps.close();
	}
	
	/*
	 * Helper method to load airlines for equipment type program.
	 */
	private void loadAirlines(Collection<EquipmentType> eTypes) throws SQLException {
		Map<String, EquipmentType> types = CollectionUtils.createMap(eTypes, "name");
		prepareStatementWithoutLimits("SELECT EQTYPE, OWNER, AIRLINE FROM common.EQAIRLINES");
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				EquipmentType eq = types.get(rs.getString(1));
				String ownerCode = rs.getString(2);
				if ((eq != null) && (eq.getOwner().getCode().equals(ownerCode)))
					eq.addAirline(SystemData.getApp(rs.getString(3)));
			}
		}
		
		_ps.close();
	}
	
	/*
	 * Helper method to load program sizes.
	 */
	private void loadSize(Collection<EquipmentType> eTypes, String db) throws SQLException {
		Map<String, EquipmentType> types = CollectionUtils.createMap(eTypes, "name");
		prepareStatementWithoutLimits("SELECT P.EQTYPE, COUNT(P.ID) FROM " + db + ".PILOTS P WHERE ((P.STATUS=?) OR "
				+ "(P.STATUS=?)) GROUP BY P.EQTYPE");
		_ps.setInt(1, Pilot.ACTIVE);
		_ps.setInt(2, Pilot.ON_LEAVE);
		
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				EquipmentType eq = types.get(rs.getString(1));
				if (eq != null)
					eq.setSize(rs.getInt(2));
			}
		}
			
		_ps.close();
	}

	/*
	 * Helper method to iterate through the result set.
	 */
	private List<EquipmentType> execute() throws SQLException {
		List<EquipmentType> results = new ArrayList<EquipmentType>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {	
				EquipmentType eq = new EquipmentType(rs.getString(1));
				eq.setCPID(rs.getInt(2));
				eq.addRanks(rs.getString(3), ",");
				eq.setActive(rs.getBoolean(4));
				eq.setNewHires(rs.getBoolean(5));
				eq.setIsDefault(rs.getBoolean(6));
				eq.setPromotionLegs(rs.getInt(7));
				eq.setPromotionHours(rs.getInt(8));
				eq.setACARSPromotionLegs(rs.getBoolean(9));
				eq.setPromotionMinLength(rs.getInt(10));
				eq.setPromotionSwitchLength(rs.getInt(11));
				eq.setMinimum1XTime(rs.getInt(12));
				eq.setMaximumAccelTime(rs.getInt(13));
				eq.setOwner(SystemData.getApp(rs.getString(14)));
				eq.setStage(rs.getInt(15));
				eq.setCPName(rs.getString(16));
				eq.setCPEmail(rs.getString(17));
				results.add(eq);
			}
		}

		_ps.close();
		return results;
	}
}