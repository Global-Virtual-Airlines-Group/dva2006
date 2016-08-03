// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012, 2013, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;

import org.deltava.util.*;

/**
 * A Data Access Object to get Pilots from the database, for use in roster operations.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GetPilot extends PilotReadDAO {

	/**
	 * Creates the DAO from a JDBC connection.
	 * @param c the JDBC connection to use
	 */
	public GetPilot(Connection c) {
		super(c);
	}
	
	/**
	 * Gets the newest pilots (with the highest pilot IDs).
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getNewestPilots() throws DAOException {
		try {
			prepareStatement("SELECT * FROM PILOTS WHERE (PILOT_ID IS NOT NULL) ORDER BY PILOT_ID DESC");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Gets a Pilot based on a pilot code. <i>This populates ratings and roles. </i>
	 * @param pilotCode the pilot code to search for (eg. 123 for DVA123).
	 * @param dbName the database name
	 * @return the Pilot object, or null if the pilot code was not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Pilot getPilotByCode(int pilotCode, String dbName) throws DAOException {

		// Build the SQL statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE), S.EXT, S.MODIFIED FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".PILOTS P LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".SIGNATURES S ON (P.ID=S.ID) WHERE (P.PILOT_ID=?) GROUP BY P.ID");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, pilotCode);

			// Execute the query and get the result
			List<Pilot> results = execute();
			if (results.isEmpty()) return null;
			
			// Update airline code
			Pilot result = results.get(0);
			result.setPilotCode(dbName + String.valueOf(result.getPilotNumber()));

			// Add roles/ratings
			loadChildRows(result, dbName);

			// Add the result to the cache and return
			_cache.add(result);
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Active and On Leave Pilots.
	 * @param orderBy the database column to sort the results by
	 * @return a List of all active/on leave Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getActivePilots(String orderBy) throws DAOException {

		StringBuilder sql = new StringBuilder("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1) AS HOURS, MAX(F.DATE) AS LASTFLIGHT FROM PILOTS P "
			+ "LEFT JOIN PIREPS F ON ((F.STATUS=?) AND (P.ID=F.PILOT_ID)) WHERE (P.STATUS=?) AND (P.PILOT_ID > 0) GROUP BY P.ID ORDER BY ");

		// Add sort by column
		sql.append((orderBy != null) ? orderBy.toUpperCase() : "P.PILOT_ID");
		try {
			prepareStatement(sql.toString());
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, Pilot.ACTIVE);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all active Pilots with a particular rank in a particular equipment program.
	 * @param eq the EquipmentType bean
	 * @param sortBy an optional sort SQL snippet
	 * @param showActive TRUE if only active pilots should be displayed, otherwise FALSE
	 * @param rank the Rank
	 * @return a List of Pilots in a particular equipment type
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Pilot> getPilotsByEQ(EquipmentType eq, String sortBy, boolean showActive, Rank rank) throws DAOException {
		
		// Build the SQL statement
		String dbName = formatDBName(eq.getOwner().getDB());
		StringBuilder sqlBuf = new StringBuilder("SELECT P.ID FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PILOTS P WHERE (P.EQTYPE=?)");
		if (showActive)
			sqlBuf.append(" AND (P.STATUS=?)");
		if (rank != null)
			sqlBuf.append(" AND (P.RANK=?)");
		
		sqlBuf.append(" ORDER BY ");
		sqlBuf.append((sortBy == null) ? "P.LASTNAME, P.FIRSTNAME" : sortBy);

		try {
			int pos = 0;
			prepareStatement(sqlBuf.toString());	
			_ps.setString(++pos, eq.getName());
			if (showActive)
				_ps.setInt(++pos, Pilot.ACTIVE);
			if (rank != null)
				_ps.setString(++pos, rank.getName());
			
			return getByID(executeIDs(), dbName + ".PILOTS").values();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Active pilots with a particular rank.
	 * @param rank the rank
	 * @return a List of active Pilots with a particular rank
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getPilotsByRank(Rank rank) throws DAOException {
		try {
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE), S.EXT, S.MODIFIED FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) "
				+ "LEFT JOIN SIGNATURES S ON (P.ID=S.ID) WHERE (P.RANK=?) AND (P.STATUS=?) GROUP BY P.ID");
			_ps.setInt(1, FlightReport.OK);
			_ps.setString(2, rank.getName());
			_ps.setInt(3, Pilot.ACTIVE);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Pilots whose last name begins with a particular letter.
	 * @param letter the first Letter of the last name
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if letter isn't a letter according to {@link Character#isLetter(char) }
	 * @throws NullPointerException if letter is null
	 */
	public List<Pilot> getPilotsByLetter(String letter) throws DAOException {

		// Check the letter
		if (StringUtils.isEmpty(letter) || !Character.isLetter(letter.charAt(0)))
			throw new IllegalArgumentException("Invalid Lastname Letter - " + letter);

		try {
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) WHERE "
				+ "(LEFT(P.LASTNAME, 1)=?) GROUP BY P.ID ORDER BY P.LASTNAME");
			_ps.setInt(1, FlightReport.OK);
			_ps.setString(2, letter.substring(0, 1).toUpperCase());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Pilots based upon their status.
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getPilotsByStatus(int status) throws DAOException {
		try {
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) WHERE "
				+ "(P.STATUS=?) GROUP BY P.ID ORDER BY P.CREATED");
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, status);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Pilots.
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getPilots() throws DAOException {
		try {
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) GROUP BY P.ID");
			_ps.setInt(1, FlightReport.OK);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Searches for Pilots matching certain name criteria, using a SQL LIKE search.
	 * @param dbName the database name
	 * @param fName the Pilot's First Name, containing wildcards accepted by the JDBC implementation's LIKE operator
	 * @param lName the Pilot's Last Name, containing wildcards accepted by the JDBC implementation's LIKE operator
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> search(String dbName, String fName, String lName) throws DAOException {
		return search(dbName, fName, lName, null, null);
	}

	/**
	 * Searches for Pilots matching certain search criteria, using a SQL LIKE search.
	 * @param dbName the database name
	 * @param fName the Pilot's First Name, containing wildcards accepted by the JDBC implementation's LIKE operator
	 * @param lName the Pilot's Last Name, containing wildcards accepted by the JDBC implementation's LIKE operator
	 * @param eMail the Pilot's e-mail Address, , containing wildcards accepted by the JDBC implementation's LIKE operator
	 * @param ratings a Collection of Ratings which the Pilots need to have
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> search(String dbName, String fName, String lName, String eMail, Collection<String> ratings) throws DAOException {
		String db = formatDBName(dbName);
		
		// Add parameters if they are non-null
		List<String> searchTerms = new ArrayList<String>();
		if (fName != null)
			searchTerms.add("(P.FIRSTNAME LIKE ?)");
		if (lName != null)
			searchTerms.add("(P.LASTNAME LIKE ?)");
		if (eMail != null)
			searchTerms.add("(P.EMAIL LIKE ?)");
		
        // If no search terms specified, return an empty list
        if (searchTerms.isEmpty() && CollectionUtils.isEmpty(ratings))
           return Collections.emptyList();
        
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.ID, COUNT(DISTINCT R.RATING) AS RTGS FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".RATINGS R, ");
		sqlBuf.append(db);	
		sqlBuf.append(".PILOTS P WHERE (P.ID=R.ID) AND ");
		
        // Aggregate the search terms
        if (!CollectionUtils.isEmpty(ratings)) {
        	StringBuilder rBuf = new StringBuilder("(");
			for (Iterator<String> ri = ratings.iterator(); ri.hasNext(); ) {
				ri.next();
				rBuf.append("(R.RATING=?)");
				if (ri.hasNext())
					rBuf.append(" OR ");
			}
        	
        	rBuf.append(')');
        	searchTerms.add(rBuf.toString());
        }
        
		// Complete the SQL statement and include ratings
        sqlBuf.append(StringUtils.listConcat(searchTerms, " AND "));
		sqlBuf.append(" GROUP BY P.ID");
		if (!CollectionUtils.isEmpty(ratings))
			sqlBuf.append(" HAVING (RTGS=?)");

		Collection<Integer> IDs = new HashSet<Integer>();
		try {
			prepareStatement(sqlBuf.toString()); int idx = 0;
			if (fName != null)
				_ps.setString(++idx, fName);
			if (lName != null)
				_ps.setString(++idx, lName);
			if (eMail != null)
				_ps.setString(++idx, eMail);
			if (!CollectionUtils.isEmpty(ratings)) {
				for (String rating : ratings)
					_ps.setString(++idx, rating);
				
				_ps.setInt(++idx, ratings.size());
			}

			// Load IDs
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					IDs.add(Integer.valueOf(rs.getInt(1)));
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		return new ArrayList<Pilot>(getByID(IDs, db + ".PILOTS").values());
	}
}