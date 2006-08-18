// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to obtain user Directory information for Pilots.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetPilotDirectory extends PilotReadDAO implements PersonUniquenessDAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetPilotDirectory(Connection c) {
		super(c);
	}

	/**
	 * Gets a Pilot based on the directory name. <i>This populates roles/ratings.</i>
	 * @param directoryName the JNDI directory name to search for (eg. cn=Luke Kolin,ou=DVA,o=SCE)
	 * @return the Pilot object, or null if the pilot code was not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public final Pilot getFromDirectory(String directoryName) throws DAOException {
		try {
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
					+ "MAX(F.DATE), S.EXT FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN "
					+ "SIGNATURES S ON (P.ID=S.ID) WHERE (UPPER(P.LDAP_DN)=?) GROUP BY P.ID");
			_ps.setInt(1, FlightReport.OK);
			_ps.setString(2, directoryName.toUpperCase());

			// Execute the query and get the result
			List results = execute();
			Pilot result = (results.size() == 0) ? null : (Pilot) results.get(0);
			if (result == null)
				return null;

			// Add roles/ratings
			addRatings(result, SystemData.get("airline.db"));
			addRoles(result, SystemData.get("airline.db"));

			// Add the result to the cache and return
			_cache.add(result);
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the Pilot with a particular Pilot Code.
	 * @param pilotCode the Pilot Code (eg DVA043)
	 * @return the Directory Name of the Pilot, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Pilot getByCode(String pilotCode) throws DAOException {

		// Parse the pilot code
		StringBuilder code = new StringBuilder();
		for (int x = 0; x < pilotCode.length(); x++) {
			char c = pilotCode.charAt(x);
			if (Character.isDigit(c))
				code.append(c);
		}

		// If we have no numbers, then abort
		if (code.length() == 0)
			return null;

		try {
			prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
					+ "MAX(F.DATE), S.EXT FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN "
					+ "SIGNATURES S ON (P.ID=S.ID) WHERE (P.PILOT_ID=?) GROUP BY P.ID");
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, Integer.parseInt(code.toString()));

			// Execute the query and get return value
			List<Pilot> results = execute();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Checks if a Person is unique, by checking the first/last names and the e-mail address.
	 * @param p the Person
	 * @param dbName the database to search
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> checkUnique(Person p, String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS WHERE (((FIRSTNAME=?) AND (LASTNAME=?)) OR (EMAIL=?))");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, p.getFirstName());
			_ps.setString(2, p.getLastName());
			_ps.setString(3, p.getEmail());

			// Build the result collection
			Set<Integer> results = new HashSet<Integer>();

			// Execute the query
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
	 * Returns all Pilots who have a particular security role.
	 * @param roleName the role name
	 * @param dbName the database name
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getByRole(String roleName, String dbName) throws DAOException {

		// Build the SQL statement
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), "
				+ "ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE) FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PILOTS P LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PIREPS F ON (P.ID=F.PILOT_ID) LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".ROLES R ON (P.ID=R.ID) WHERE (R.ROLE=?) AND (F.STATUS=?) GROUP BY P.ID");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, roleName);
			_ps.setInt(2, FlightReport.OK);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Performs a soundex search on a Person's full name to detect possible matches. The soundex implementation is
	 * dependent on the capabilities of the underlying database engine, and is not guaranteed to be consistent (or even
	 * supported) across different database servers.
	 * @param usr the Person to check for
	 * @param dbName the database name
	 * @return a Collection of Database IDs as Integers
	 */
	public Collection<Integer> checkSoundex(Person usr, String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder(
				"SELECT ID, SOUNDEX(?) AS TARGET, SOUNDEX(CONCAT(FIRSTNAME, LASTNAME)) " + "AS SX FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS P WHERE (ID<>?)");

		// If we're checking for an applicant, remove its PilotID
		int appPilotID = 0;
		if (usr instanceof Applicant) {
			Applicant a = (Applicant) usr;
			appPilotID = a.getPilotID();
			if (appPilotID > 0)
				sqlBuf.append(" AND (ID<>?)");
		}

		sqlBuf.append(" HAVING ((LEFT(SX, LENGTH(TARGET))=TARGET) OR (LEFT(TARGET, LENGTH(SX))=SX)) ORDER BY ID");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, usr.getName());
			_ps.setInt(2, usr.getID());
			if (appPilotID > 0)
				_ps.setInt(3, appPilotID);

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			Collection<Integer> results = new ArrayList<Integer>();
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
	 * Searches for a Pilot name (or fragment thereof) and returns the database IDs of Pilots matching the name.
	 * @param fullName the full name or fragment
	 * @param dbName the database name
	 * @param doFragment TRUE if a partial match will be accepted, otherwise FALSE
	 * @return a Collection of database IDs as Integers
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> search(String fullName, String dbName, boolean doFragment) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.ID FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS P WHERE (UPPER(CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME))");
		sqlBuf.append(doFragment ? " LIKE ?)" : "=?)");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, doFragment ? "%" + fullName.toUpperCase() + "%" : fullName.toUpperCase());

			// Execute the query
			Collection<Integer> results = new ArrayList<Integer>();
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
}