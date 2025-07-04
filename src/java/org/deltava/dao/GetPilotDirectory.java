// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2014, 2018, 2019, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to obtain user Directory information for Pilots.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class GetPilotDirectory extends GetPilot implements PersonUniquenessDAO {
	
	private static final Cache<CacheableLong> _idCache = CacheManager.get(CacheableLong.class, "PilotExternalID");

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetPilotDirectory(Connection c) {
		super(c);
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
		if (code.length() == 0) return null;
		try (PreparedStatement ps =  prepare("SELECT ID FROM PILOTS WHERE (PILOT_ID=?) LIMIT 1")) {
			ps.setInt(1, Integer.parseInt(code.toString()));
			Collection<Pilot> pilots = getByID(executeIDs(ps), "PILOTS").values();
			return pilots.stream().findFirst().orElse(null);
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
	@Override
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
	@Override
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

		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			int param = 0;
			ps.setString(++param, p.getFirstName());
			ps.setString(++param, p.getLastName());
			if (!StringUtils.isEmpty(p.getEmail()))
				ps.setString(++param, p.getEmail());
			if (days > 0)
				ps.setInt(++param, days);
			
			return executeIDs(ps);
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
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, usr.getID());
			ps.setString(2, usr.getEmail());
			return executeIDs(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns users from the current database based on their Instant Message address.
	 * @param addr the address
	 * @return a Pilot, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Pilot getByIMAddress(String addr) throws DAOException {
		
		// Check the cache
		CacheableLong id = _idCache.get(addr);
		if (id != null)
			return get(id.intValue());
		
		try (PreparedStatement ps = prepare("SELECT ID FROM PILOT_IMADDR WHERE (ADDR=?)")) {
			ps.setString(1, addr);
			Collection<Integer> IDs = executeIDs(ps);
			if (IDs.isEmpty()) return null;
			int dbid = IDs.stream().findFirst().get().intValue();
			_idCache.add(new CacheableLong(addr, dbid));
			return get(dbid);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Pilots with permanent accounts.
	 * @return a Collection of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Pilot> getPermanent() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT ID FROM PILOTS WHERE (PERMANENT=?)")) {
			ps.setBoolean(1, true);
			return getByID(executeIDs(ps), "PILOTS").values();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all active Pilots who have a particular security role.
	 * @param roleName the role name
	 * @param dbName the database name
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getByRole(String roleName, String dbName) throws DAOException {
		return getByRole(roleName, dbName, true);
	}

	/**
	 * Returns all Pilots who have a particular security role.
	 * @param roleName the role name
	 * @param dbName the database name
	 * @param activeOnly TRUE to only include active Pilots, otherwise FALSE
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Pilot> getByRole(String roleName, String dbName, boolean activeOnly) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.ID FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS P LEFT JOIN common.AUTH_ROLES R ON (P.ID=R.ID) WHERE (R.ROLE=?) ");
		if (activeOnly)
			sqlBuf.append("AND (P.STATUS=?) ");
		
		sqlBuf.append("GROUP BY P.ID");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, roleName);
			if (activeOnly)
				ps.setInt(2, PilotStatus.ACTIVE.ordinal());
			
			return new ArrayList<Pilot>(getByID(executeIDs(ps), "PILOTS").values());
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
	@Override
	public Collection<Integer> checkSoundex(Person usr, String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, SOUNDEX(?) AS TARGET, SOUNDEX(LASTNAME) AS SX FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS WHERE (ID<>?)");

		// If we're checking for an applicant, remove its PilotID
		int appPilotID = 0;
		if (usr instanceof Applicant a) {
			appPilotID = a.getPilotID();
			if (appPilotID > 0)
				sqlBuf.append(" AND (ID<>?)");
		}

		sqlBuf.append(" HAVING ((LEFT(SX, LENGTH(TARGET))=TARGET) OR (LEFT(TARGET, LENGTH(SX))=SX)) ORDER BY ID");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, usr.getLastName());
			ps.setInt(2, usr.getID());
			if (appPilotID > 0)
				ps.setInt(3, appPilotID);

			return executeIDs(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}