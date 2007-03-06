// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A DAO to get Pilot object(s) from the database, for use in roster operations.
 * @author Luke
 * @version 1.0
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
	 * Removes a Pilot from the cache to ensure a fresh read from the database.
	 * @param id the Pilot's database ID
	 * @see PilotDAO#invalidate(int)
	 */
	public static void invalidate(int id) {
		invalidate(id);
	}

	/**
	 * Returns the location of the specified Pilot.
	 * @param pilotID the Pilot's database ID
	 * @return a GeoLocation with the pilot's location, or null if none specified
	 * @throws DAOException
	 */
	public GeoLocation getLocation(int pilotID) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT LAT, LNG FROM PILOT_MAP WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			
			// Execute the query and get results
			ResultSet rs = _ps.executeQuery();
			GeoLocation gl = (rs.next()) ? new GeoPosition(rs.getDouble(1), rs.getDouble(2)) : null;
			setQueryMax(0);
			
			// Clean up and return
			rs.close();
			_ps.close();
			return gl;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the locations of pilots who have signed up for the Pilot location board.
	 * @return a Map of GeoLocation objects, keyed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, GeoLocation> getPilotBoard() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT M.* FROM PILOT_MAP M, PILOTS P WHERE (M.ID=P.ID) AND (P.STATUS=?)");
			_ps.setInt(1, Pilot.ACTIVE);

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the result set
			Map<Integer, GeoLocation> results = new HashMap<Integer, GeoLocation>();
			while (rs.next()) {
				GeoPosition gp = new GeoPosition(rs.getDouble(2), rs.getDouble(3));
				results.put(new Integer(rs.getInt(1)), gp);
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
	 * Gets the newest pilots (with the highest pilot IDs).
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getNewestPilots() throws DAOException {
		try {
			prepareStatement("SELECT P.* FROM PILOTS P WHERE (P.PILOT_ID IS NOT NULL) ORDER BY PILOT_ID DESC");
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
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), "
				+ "ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE), S.EXT FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PILOTS P LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".SIGNATURES S ON (P.ID=S.ID) WHERE (P.PILOT_ID=?) GROUP BY P.ID");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, pilotCode);

			// Execute the query and get the result
			List results = execute();
			Pilot result = (results.size() == 0) ? null : (Pilot) results.get(0);
			if (result == null)
				return null;
			
			// Update airline code
			result.setPilotCode(dbName.toUpperCase() + String.valueOf(result.getPilotNumber()));

			// Add roles/ratings
			addRatings(result, dbName);
			addRoles(result, dbName);

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

		StringBuilder sql = new StringBuilder("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), "
				+ "ROUND(SUM(F.FLIGHT_TIME), 1) AS HOURS, MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON "
				+ "((F.STATUS=?) AND (P.ID=F.PILOT_ID)) WHERE (P.STATUS=?) AND (P.PILOT_ID > 0) "
				+ "GROUP BY P.ID ORDER BY ");

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
	 * Returns all active pilots within an equipment type program.
	 * @param eqType the equipment type
	 * @return a List of Pilots in a particular equipment type
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getPilotsByEQ(String eqType, boolean showActive) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), "
				+ "ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) "
				+ "AND (F.STATUS=?)) WHERE (P.EQTYPE=?)");
		if (showActive)
			sqlBuf.append(" AND (P.STATUS=?)");
				
		sqlBuf.append("GROUP BY P.ID ORDER BY P.LASTNAME, P.FIRSTNAME");
		
		try {
			prepareStatement(sqlBuf.toString());	
			_ps.setInt(1, FlightReport.OK);
			_ps.setString(2, eqType);
			if (showActive)
				_ps.setInt(3, Pilot.ACTIVE);
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all active Pilots with a particular rank in a particular equipment program.
	 * @param rank the rank
	 * @param eqType the equipment type
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getPilotsByEQRank(String rank, String eqType) throws DAOException {

		// Get all the pilots with the rank (since this is usually used to filter out ACPs/CPs)
		List<Pilot> pilots = getPilotsByRank(rank);
		for (Iterator<Pilot> i = pilots.iterator(); i.hasNext();) {
			Pilot p = i.next();
			if (!eqType.equals(p.getEquipmentType()))
				i.remove();
		}

		return pilots;
	}

	/**
	 * Returns all Active pilots with a particular rank.
	 * @param rank the rank
	 * @return a List of active Pilots with a particular rank
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getPilotsByRank(String rank) throws DAOException {
		try {
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
					+ "MAX(F.DATE), S.EXT FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN "
					+ "SIGNATURES S ON (P.ID=S.ID) WHERE (P.RANK=?) AND (P.STATUS=?) GROUP BY P.ID");
			_ps.setInt(1, FlightReport.OK);
			_ps.setString(2, rank);
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
		if (!Character.isLetter(letter.charAt(0)))
			throw new IllegalArgumentException("Invalid Lastname Letter - " + letter);

		// Init the prepared statement
		try {
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
					+ "MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) WHERE "
					+ "(UPPER(LEFT(P.LASTNAME, 1))=?) GROUP BY P.ID ORDER BY P.LASTNAME");
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
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
					+ "MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) WHERE "
					+ "(P.STATUS=?) GROUP BY P.ID");
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
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
					+ "MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) GROUP BY P.ID");
			_ps.setInt(1, FlightReport.OK);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Searches for Pilots matching certain search criteria, using a SQL LIKE search.
	 * @param dbName the database name
	 * @param fName the Pilot's First Name, containing wildcards accepted by the JDBC implementation's LIKE operator
	 * @param lName the Pilot's Last Name, containing wildcards accepted by the JDBC implementation's LIKE operator
	 * @param eMail the Pilot's e-mail Address, , containing wildcards accepted by the JDBC implementation's LIKE
	 * operator
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> search(String dbName, String fName, String lName, String eMail) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), "
				+ "ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE) FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS P LEFT JOIN ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PIREPS F ON (P.ID=F.PILOT_ID) WHERE ");

		// Add parameters if they are non-null
		List<String> searchTerms = new ArrayList<String>();
		if (fName != null)
			searchTerms.add("(P.FIRSTNAME LIKE ?)");
		if (lName != null)
			searchTerms.add("(P.LASTNAME LIKE ?)");
		if (eMail != null)
			searchTerms.add("(P.EMAIL LIKE ?)");
        
        // If no search terms specified, return an empty list
        if (searchTerms.isEmpty())
           return Collections.emptyList();
        
        // Aggregate the search terms
		for (Iterator i = searchTerms.iterator(); i.hasNext();) {
			String srchTerm = (String) i.next();
			sqlBuf.append(srchTerm);
			if (i.hasNext())
				sqlBuf.append(" AND ");
		}

		// Complete the SQL statement
		sqlBuf.append(" GROUP BY P.ID");

		List<Pilot> results = null;
		try {
			prepareStatement(sqlBuf.toString());

			// Init the statements
			int idx = 0;
			if (fName != null)
				_ps.setString(++idx, fName);
			if (lName != null)
				_ps.setString(++idx, lName);
			if (eMail != null)
				_ps.setString(++idx, eMail);

			// Execute the Query and return
			results = execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Update pilot codes and return
		updatePilotCodes(results, dbName);
		return results;
	}
}