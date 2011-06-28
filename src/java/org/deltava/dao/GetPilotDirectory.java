// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to obtain user Directory information for Pilots.
 * @author Luke
 * @version 4.0
 * @since 1.0
 */

public class GetPilotDirectory extends GetPilot implements PersonUniquenessDAO {

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
				+ "MAX(F.DATE), S.EXT, S.MODIFIED FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) "
				+ "LEFT JOIN SIGNATURES S ON (P.ID=S.ID) WHERE (UPPER(P.LDAP_DN)=?) GROUP BY P.ID");
			_ps.setInt(1, FlightReport.OK);
			_ps.setString(2, directoryName.toUpperCase());

			// Execute the query and get the result
			List<Pilot> results = execute();
			Pilot result = (results.size() == 0) ? null : results.get(0);
			if (result == null)
				return null;

			// Add roles/ratings
			loadChildRows(result, SystemData.get("airline.db"));

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
				+ "MAX(F.DATE), S.EXT, S.MODIFIED FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) "
				+ "LEFT JOIN SIGNATURES S ON (P.ID=S.ID) WHERE (P.PILOT_ID=?) GROUP BY P.ID");
			_ps.setInt(1, FlightReport.OK);
			_ps.setInt(2, Integer.parseInt(code.toString()));

			// Execute the query and get return value
			List<Pilot> results = execute();
			Pilot result = (results.size() == 0) ? null : results.get(0);
			if (result == null)
				return null;
			
			// Add roles/ratings
			loadChildRows(result, SystemData.get("airline.db"));
			return result;
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
		return checkUnique(p, dbName, -1);
	}

	/**
	 * Checks if a Person is unique, by checking the first/last names and the e-mail address.
	 * @param p the Person
	 * @param dbName the database to search
	 * @param days restrict uniqueness search to users created in the last number of days, or -1 for all
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> checkUnique(Person p, String dbName, int days) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS WHERE (((FIRSTNAME=?) AND (LASTNAME=?))");
		if (!StringUtils.isEmpty(p.getEmail()))
			sqlBuf.append("OR (EMAIL=?)");
		sqlBuf.append(')');
		if (days > 0)
			sqlBuf.append(" AND (CREATED > DATE_SUB(CURDATE(), INTERVAL ? DAY))");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			int param = 0;
			_ps.setString(++param, p.getFirstName());
			_ps.setString(++param, p.getLastName());
			if (!StringUtils.isEmpty(p.getEmail()))
				_ps.setString(++param, p.getEmail());
			if (days > 0)
				_ps.setInt(++param, days);
			
			return executeIDs();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Checks whether a particular e-mail address is unique.
	 * @param usr the Person to check for
	 * @param dbName the database to check
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> checkUniqueEMail(Person usr, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS WHERE (ID<>?) AND (EMAIL=?)");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, usr.getID());
			_ps.setString(2, usr.getEmail());
			return executeIDs();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns users from the current database based on their Instant Message address.
	 * @param addr the address
	 * @return a Map of Pilots, keyed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Pilot> getByIMAddress(String addr) throws DAOException {
		try {
			prepareStatement("SELECT ID FROM PILOT_IMADDR WHERE (ADDR=?)");
			_ps.setString(1, addr);
			return getByID(executeIDs(), "PILOTS");
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns users from the current database based on their Instant Message type.
	 * @param addr the address type
	 * @return a Map of Pilots, keyed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Pilot> getByIMType(IMAddress addr) throws DAOException {
		try {
			prepareStatement("SELECT ID FROM PILOT_IMADDR WHERE (TYPE=?)");
			_ps.setString(1, addr.toString());
			return getByID(executeIDs(), "PILOTS");
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
		StringBuilder sqlBuf = new StringBuilder("SELECT P.ID FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PILOTS P LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".ROLES R ON (P.ID=R.ID) WHERE (R.ROLE=?) GROUP BY P.ID");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, roleName);
			return new ArrayList<Pilot>(getByID(executeIDs(), "PILOTS").values());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Performs a soundex search on a Person's last name to detect possible matches. The soundex implementation is
	 * dependent on the capabilities of the underlying database engine, and is not guaranteed to be consistent (or even
	 * supported) across different database servers.
	 * @param usr the Person to check for
	 * @param dbName the database name
	 * @return a Collection of Database IDs as Integers
	 */
	public Collection<Integer> checkSoundex(Person usr, String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder(
				"SELECT ID, SOUNDEX(?) AS TARGET, SOUNDEX(LASTNAME) AS SX FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS WHERE (ID<>?)");

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
			_ps.setString(1, usr.getLastName());
			_ps.setInt(2, usr.getID());
			if (appPilotID > 0)
				_ps.setInt(3, appPilotID);

			return executeIDs();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}