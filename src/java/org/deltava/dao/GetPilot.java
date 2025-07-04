// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012, 2013, 2015, 2016, 2017, 2018, 2019, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;

import org.deltava.util.*;

/**
 * A Data Access Object to get Pilots from the database, for use in roster operations.
 * @author Luke
 * @version 11.1
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
		try (PreparedStatement ps = prepare("SELECT * FROM PILOTS WHERE (PILOT_ID IS NOT NULL) ORDER BY PILOT_ID DESC")) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns pilots who have enabled currency-based Check Rides.
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Pilot> getCurrencyPilots() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT ID FROM PILOTS WHERE (PROF_CR=?) AND (STATUS=?)")) {
			ps.setBoolean(1, true);
			ps.setInt(2, PilotStatus.ACTIVE.ordinal());
			return getByID(executeIDs(ps), "PILOTS").values();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Gets a Pilot based on e-mail address.
	 * @param eMail the e-mail address
	 * @return a Pilot, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Pilot getByEMail(String eMail) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT ID FROM PILOTS WHERE (EMAIL=?) LIMIT 1")) {
			ps.setString(1, eMail);
			List<Integer> IDs = executeIDs(ps);
			return IDs.isEmpty() ? null : get(IDs.get(0).intValue());
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

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setInt(2, pilotCode);

			// Execute the query and get the result
			List<Pilot> results = execute(ps);
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
	public List<Integer> getActivePilots(String orderBy) throws DAOException {

		StringBuilder sql = new StringBuilder("SELECT ID FROM PILOTS WHERE (STATUS=?) AND (PILOT_ID>0) ORDER BY ");
		sql.append((orderBy != null) ? orderBy.toUpperCase() : "PILOT_ID");
		try (PreparedStatement ps = prepare(sql.toString())) {
			ps.setInt(1, PilotStatus.ACTIVE.ordinal());
			return executeIDs(ps);
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
	public List<Pilot> getPilotsByEQ(EquipmentType eq, String sortBy, boolean showActive, Rank rank) throws DAOException {
		
		// Build the SQL statement
		String dbName = formatDBName(eq.getOwner().getDB());
		StringBuilder sqlBuf = new StringBuilder("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1) AS HOURS, MAX(F.DATE) AS LASTFLIGHT FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PILOTS P LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) WHERE (P.EQTYPE=?)");
		if (showActive)
			sqlBuf.append(" AND (P.STATUS=?)");
		if (rank != null)
			sqlBuf.append(" AND (P.RANKING=?)");
		
		sqlBuf.append("GROUP BY P.ID ORDER BY ");
		sqlBuf.append((sortBy == null) ? "P.LASTNAME, P.FIRSTNAME" : sortBy);

		try (PreparedStatement ps = prepare(sqlBuf.toString())) { 
			int pos = 0;
			ps.setInt(++pos, FlightStatus.OK.ordinal());
			ps.setString(++pos, eq.getName());
			if (showActive)
				ps.setInt(++pos, PilotStatus.ACTIVE.ordinal());
			if (rank != null)
				ps.setInt(++pos, rank.ordinal());
			
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE), S.EXT, S.MODIFIED FROM PILOTS P LEFT JOIN PIREPS F "
			+ "ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN SIGNATURES S ON (P.ID=S.ID) WHERE (P.RANKING=?) AND (P.STATUS=?) GROUP BY P.ID")) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setInt(2, rank.ordinal());
			ps.setInt(3, PilotStatus.ACTIVE.ordinal());
			return execute(ps);
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

		char c = Character.toUpperCase(letter.charAt(0));
		try (PreparedStatement ps = prepare("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) "
			+ "AND (F.STATUS=?)) WHERE (P.LASTNAME>=?) AND (P.LASTNAME<?) AND (P.FORGOTTEN=?) GROUP BY P.ID ORDER BY P.LASTNAME, P.FIRSTNAME, P.ID")) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setString(2, Character.toString(c));
			ps.setString(3, Character.toString(++c));
			ps.setBoolean(4, false);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Pilots based upon their status.
	 * @param status a PilotStatus
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getPilotsByStatus(PilotStatus status) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) "
			+ "AND (F.STATUS=?)) WHERE (P.STATUS=?) AND (P.FORGOTTEN=?) GROUP BY P.ID ORDER BY P.CREATED")) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setInt(2, status.ordinal());
			ps.setBoolean(3, false);
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) "
			+ "AND (F.STATUS=?)) WHERE (P.FORGOTTEN=?) GROUP BY P.ID")) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setBoolean(2, false);
			return execute(ps);
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
		sqlBuf.append(".PILOTS P WHERE (P.ID=R.ID) AND (P.FORGOTTEN=?) AND ");
		
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
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			int idx = 1;
			ps.setBoolean(1, false);
			if (fName != null)
				ps.setString(++idx, fName);
			if (lName != null)
				ps.setString(++idx, lName);
			if (eMail != null)
				ps.setString(++idx, eMail);
			if (!CollectionUtils.isEmpty(ratings)) {
				for (String rating : ratings)
					ps.setString(++idx, rating);
				
				ps.setInt(++idx, ratings.size());
			}

			// Load IDs
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					IDs.add(Integer.valueOf(rs.getInt(1)));
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		return new ArrayList<Pilot>(getByID(IDs, db + ".PILOTS").values());
	}
}