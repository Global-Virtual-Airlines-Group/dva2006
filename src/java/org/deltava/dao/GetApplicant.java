// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.system.UserData;

import org.deltava.util.CollectionUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to read Applicant data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetApplicant extends DAO {

	private static final Logger log = Logger.getLogger(GetApplicant.class);
	static final Cache _cache = new AgingCache(4); // Package private so set DAO can update

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetApplicant(Connection c) {
		super(c);
	}

	/**
	 * Loads an Applicant from the database.
	 * @param id the Applicant's database ID
	 * @return an Applicant, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Applicant get(int id) throws DAOException {
		
		// Check if we're in the cache
		Applicant a = (Applicant) _cache.get(new Integer(id));
		if (a != null)
			return a;

		try {
			prepareStatement("SELECT * FROM APPLICANTS WHERE (ID=?)");
			_ps.setInt(1, id);
			setQueryMax(1);

			// Get results, return first or null
			List results = execute();
			return results.isEmpty() ? null : (Applicant) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads an Applicant record for an existing Pilot from the database.
	 * @param pilotID the database ID of the Applicant's Pilot record
	 * @return an Applicant, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Applicant getByPilotID(int pilotID) throws DAOException {
		try {
			prepareStatement("SELECT * FROM APPLICANTS WHERE (PILOT_ID=?)");
			_ps.setInt(1, pilotID);
			setQueryMax(1);

			// Get results, return first or null
			List results = execute();
			return results.isEmpty() ? null : (Applicant) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns a Map of applicant based on a group of database IDs. This is typically called by a Water Cooler thread/channel
	 * list command.
	 * @param ids a Collection of database IDs. These can either be Integers, or {@link UserData} beans
	 * @param tableName the table to read from, in <i>DATABASE.TABLE</i> format for a remote database, or
	 * <i>TABLE</i> for a table in the current airline's database.
	 * @return a Map of Applicants, indexed by the pilot code
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map getByID(Collection ids, String tableName) throws DAOException {
		
		List results = new ArrayList();
		log.debug("Raw set size = " + ids.size());

		// Build the SQL statement
		StringBuffer sqlBuf = new StringBuffer("SELECT * FROM ");
		sqlBuf.append(tableName);
		sqlBuf.append(" A WHERE (A.ID IN (");
		int querySize = 0;
		for (Iterator i = ids.iterator(); i.hasNext();) {
			Object rawID = i.next();
			Integer id = (rawID instanceof Integer) ? (Integer) rawID : new Integer(((UserData) rawID).getID());

			// Pull from the cache if at all possible; this is an evil query
			Applicant a = (Applicant) _cache.get(id);
			if (a != null) {
				results.add(a);
			} else {
				querySize++;
				sqlBuf.append(id.toString());
				if (i.hasNext())
					sqlBuf.append(',');
			}
		}
		
		// Only execute the prepared statement if we haven't gotten anything from the cache
		log.debug("Uncached set size = " + querySize);
		if (querySize > 0) {
			if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
				sqlBuf.setLength(sqlBuf.length() - 1);

			sqlBuf.append("))");
			List uncached = null;
			try {
				prepareStatementWithoutLimits(sqlBuf.toString());
				uncached = execute();
			} catch (SQLException se) {
				throw new DAOException(se);
			}

			// Add to results and the cache
			results.addAll(uncached);
			_cache.addAll(uncached);
		}

		// Convert to a Map for easy searching
		return CollectionUtils.createMap(results, "ID");
	}

	/**
	 * Loads an applicant based on a directory name.
	 * @param directoryName the directory name.
	 * @return an Applicant, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Applicant getFromDirectory(String directoryName) throws DAOException {
		try {
			prepareStatement("SELECT * FROM APPLICANTS WHERE (LDAP_DN=?)");
			_ps.setString(1, directoryName);
			setQueryMax(1);

			// Get results, return first or null
			List results = execute();
			return results.isEmpty() ? null : (Applicant) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Applicants whose last name begins with a particular letter.
	 * @param letter the first Letter of the last name
	 * @return a List of Applicants
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if letter isn't a letter according to {@link Character#isLetter(char) }
	 * @throws NullPointerException if letter is null
	 */
	public List getByLetter(String letter) throws DAOException {

		// Check the letter
		if (!Character.isLetter(letter.charAt(0)))
			throw new IllegalArgumentException("Invalid Lastname Letter - " + letter);

		try {
			prepareStatement("SELECT * FROM APPLICANTS WHERE (UPPER(LEFT(LASTNAME, 1))=?) ORDER BY LASTNAME");
			_ps.setString(1, letter.substring(0, 1).toUpperCase());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Applicants with a particular status.
	 * @param status the Applicant status code
	 * @return a List of Applicants
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getByStatus(int status) throws DAOException {
		try {
			prepareStatement("SELECT * FROM APPLICANTS WHERE (STATUS=?) ORDER BY LASTNAME");
			_ps.setInt(1, status);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Applicants hired into a particular equipment program.
	 * @param eqType the equipment program code
	 * @return a List of Applicants
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getByEquipmentType(String eqType) throws DAOException {
		try {
			prepareStatement("SELECT * FROM APPLICANTS WHERE (STATUS=?) AND (EQTYPE=?) ORDER BY LASTNAME");
			_ps.setInt(1, Applicant.APPROVED);
			_ps.setString(2, eqType);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Checks if an Applicant is unique, by checking the first/last names and the e-mail address. This will not return a
	 * match against rejected Applicants.
	 * @param p the Person
	 * @param dbName the database to search
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection checkUnique(Person p, String dbName) throws DAOException {
	   
	   // Build the SQL statement
	   StringBuffer sqlBuf = new StringBuffer("SELECT ID FROM ");
	   sqlBuf.append(dbName.toLowerCase());
	   sqlBuf.append(".APPLICANTS WHERE (STATUS != ?) AND (((FIRSTNAME=?) AND (LASTNAME=?)) OR "
	         + "(EMAIL=?))");
	   
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, Applicant.REJECTED);
			_ps.setString(2, p.getFirstName());
			_ps.setString(3, p.getLastName());
			_ps.setString(4, p.getEmail());
			
			// Build result collection
			Set results = new HashSet();

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
	 * Helper method to extract data from the result set.
	 */
	private List execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		List results = new ArrayList();
		while (rs.next()) {
			Applicant a = new Applicant(rs.getString(4), rs.getString(5));
			a.setID(rs.getInt(1));
			a.setStatus(rs.getInt(3));
			a.setPilotID(rs.getInt(2)); // Status must be populated first
			a.setEmail(rs.getString(6));
			a.setLocation(rs.getString(7));
			a.setIMHandle(rs.getString(8));
			a.setNetworkID("VATSIM", rs.getString(9));
			a.setNetworkID("IVAO", rs.getString(10));
			a.setLegacyHours(rs.getDouble(11));
			a.setLegacyURL(rs.getString(12));
			a.setLegacyVerified(rs.getBoolean(13));
			a.setHomeAirport(rs.getString(14));
			a.setEquipmentType(rs.getString(15));
			a.setRank(rs.getString(16));
			a.setNotifyOption(Person.FLEET, rs.getBoolean(17));
			a.setNotifyOption(Person.EVENT, rs.getBoolean(18));
			a.setNotifyOption(Person.NEWS, rs.getBoolean(19));
			a.setEmailAccess(rs.getInt(20));
			a.setCreatedOn(rs.getTimestamp(21));
			a.setLoginCount(rs.getInt(22));
			a.setLastLogin(rs.getTimestamp(23));
			a.setLastLogoff(rs.getTimestamp(24));
			a.setLoginHost(rs.getString(25));
			a.setRegisterHostName(rs.getString(26));
			a.setDateFormat(rs.getString(27));
			a.setTimeFormat(rs.getString(28));
			a.setNumberFormat(rs.getString(29));
			a.setAirportCodeType(rs.getInt(30));
			a.setTZ(TZInfo.init(rs.getString(31)));
			a.setUIScheme(rs.getString(32));

			// Add to results and cache
			results.add(a);
			_cache.add(a);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}