// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;

/**
 * A Data Access Object to read Applicant data.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class GetApplicant extends PilotDAO implements PersonUniquenessDAO {

	private static final Logger log = Logger.getLogger(GetApplicant.class);
	private static final String NO_IP = "0.0.0.0";

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
			prepareStatementWithoutLimits("SELECT *, INET_NTOA(REGADDR) FROM APPLICANTS WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);

			// Get results, return first or null
			List<Applicant> results = execute();
			return results.isEmpty() ? null : (Applicant) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Searches for pending Applicants by first/last name. 
	 * @param fName the first name
	 * @param lName the last name
	 * @return a Collection of Applicants
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Applicant> getByName(String fName, String lName) throws DAOException {
		try {
			prepareStatement("SELECT *, INET_NTOA(REGADDR) FROM APPLICANTS WHERE (STATUS=?) AND "
					+ "(FIRSTNAME LIKE ?) AND (LASTNAME LIKE ?)");
			_ps.setInt(1, Applicant.PENDING);
			_ps.setString(2, fName);
			_ps.setString(3, lName);
			return execute();
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
			prepareStatementWithoutLimits("SELECT *, INET_NTOA(REGADDR) FROM APPLICANTS WHERE (PILOT_ID=?) LIMIT 1");
			_ps.setInt(1, pilotID);

			// Get results, return first or null
			List<Applicant> results = execute();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns a Map of applicant based on a group of database IDs. This is typically called by a Water Cooler
	 * thread/channel list command.
	 * @param ids a Collection of database IDs. These can either be Integers, or {@link UserData} beans
	 * @param tableName the table to read from, in <i>DATABASE.TABLE</i> format for a remote database, or <i>TABLE</i>
	 * for a table in the current airline's database.
	 * @return a Map of Applicants, indexed by the pilot code
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Applicant> getByID(Collection ids, String tableName) throws DAOException {

		List<Applicant> results = new ArrayList<Applicant>();
		log.debug("Raw set size = " + ids.size());

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT A.*, INET_NTOA(A.REGADDR) FROM ");
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
			List<Applicant> uncached = null;
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
	 * Returns an Applicant object which may be in another Airline's database.
	 * @param ud the UserData bean containg the Applicant location
	 * @return the Applicant bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Applicant get(UserData ud) throws DAOException {
		if (ud == null)
			return null;

		// Get a map from the table, and get the first value
		Map<Integer, Applicant> pilots = getByID(Collections.singleton(ud), ud.getDB() + "." + ud.getTable());
		Iterator<Applicant> i = pilots.values().iterator();
		return i.hasNext() ? i.next() : null;
	}

	/**
	 * Returns Applicant objects which may be in another Airline's database.
	 * @param udm the UserDataMap bean containg the Pilot locations
	 * @return a Map of Pilots indexed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Applicant> get(UserDataMap udm) throws DAOException {
		Map<Integer, Applicant> results = new HashMap<Integer, Applicant>();
		for (Iterator<String> i = udm.getTableNames().iterator(); i.hasNext(); ) {
			String tableName = i.next();
			if (!UserData.isPilotTable(tableName))
				results.putAll(getByID(udm.getByTable(tableName), tableName));
		}
		
		return results;
	}
	
	/**
	 * Loads an applicant based on a directory name.
	 * @param directoryName the directory name.
	 * @return an Applicant, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Applicant getFromDirectory(String directoryName) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT *, INET_NTOA(REGADDR) FROM APPLICANTS WHERE (LDAP_DN=?) LIMIT 1");
			_ps.setString(1, directoryName);

			// Get results, return first or null
			List<Applicant> results = execute();
			return results.isEmpty() ? null : results.get(0);
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
	public List<Applicant> getByLetter(String letter) throws DAOException {

		// Check the letter
		if (!Character.isLetter(letter.charAt(0)))
			throw new IllegalArgumentException("Invalid Lastname Letter - " + letter);

		try {
			prepareStatement("SELECT *, INET_NTOA(REGADDR) FROM APPLICANTS WHERE "
					+ "(UPPER(LEFT(LASTNAME, 1))=?) ORDER BY CREATED DESC");
			_ps.setString(1, letter.substring(0, 1).toUpperCase());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Applicants with a particular status.
	 * @param status the Applicant status code
	 * @param orderBy the sort column
	 * @return a List of Applicants
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Applicant> getByStatus(int status, String orderBy) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT *, INET_NTOA(REGADDR) FROM APPLICANTS WHERE (STATUS=?)");
		if (!StringUtils.isEmpty(orderBy)) {
			sqlBuf.append(" ORDER BY ");
			sqlBuf.append(orderBy);
		}

		try {
			prepareStatement(sqlBuf.toString());
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
	public List<Applicant> getByEquipmentType(String eqType) throws DAOException {
		try {
			prepareStatement("SELECT *, INET_NTOA(REGADDR) FROM APPLICANTS WHERE (STATUS=?) AND "
					+ "(EQTYPE=?) ORDER BY LASTNAME");
			_ps.setInt(1, Applicant.APPROVED);
			_ps.setString(2, eqType);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Checks if this IP address had been used to register in the past few days.
	 * @param addr the IP address
	 * @param days the number of days to check
	 * @return TRUE if the IP address was used, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean isIPRegistered(String addr, int days) throws DAOException {
		try {
			prepareStatement("SELECT COUNT(DISTINCT ID) FROM APPLICANTS WHERE (STATUS <> ?) AND "
					+ "(REGADDR=INET_ATON(?)) AND (CREATED > DATE_SUB(NOW(), INTERVAL ? DAY))");
			_ps.setInt(1, Applicant.REJECTED);
			_ps.setString(2, addr);
			_ps.setInt(3, Math.max(1, days));
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			boolean result = rs.next() ? (rs.getInt(1) > 0) : false;
			
			// Clean up and return
			rs.close();
			_ps.close();
			return result;
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
	public Collection<Integer> checkUnique(Person p, String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".APPLICANTS WHERE (STATUS=?) AND (((FIRSTNAME=?) AND (LASTNAME=?)) OR " + "(EMAIL=?))");

		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, Applicant.PENDING);
			_ps.setString(2, p.getFirstName());
			_ps.setString(3, p.getLastName());
			_ps.setString(4, p.getEmail());
			return executeIDs();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Searches for Applicants registering from the same TCP/IP network. If the Applicant has
	 * been hired, this will return the Pilot database ID, instead of the Applicant database ID.
	 * @param addr the network Address
	 * @param maskAddr the network Mask
	 * @param dbName the database name
	 * @return a Collection of Database IDs as Integers
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> checkAddress(String addr, String maskAddr, String dbName) throws DAOException {
		if (NO_IP.equals(addr))
			return Collections.emptyList();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT IF(PILOT_ID, PILOT_ID, ID) FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".APPLICANTS A WHERE ((INET_ATON(?) & INET_ATON(?)) = "
				+ "(REGADDR & INET_ATON(?)))");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, addr);
			_ps.setString(2, maskAddr);
			_ps.setString(3, maskAddr);
			return executeIDs();
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
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> checkSoundex(Person usr, String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, SOUNDEX(?) AS TARGET, SOUNDEX(LASTNAME) AS SX FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".APPLICANTS WHERE (ID<>?) AND (STATUS<>?) HAVING ((LEFT(SX, LENGTH(TARGET))=TARGET) OR "
				+ "(LEFT(TARGET, LENGTH(SX))=SX)) ORDER BY ID");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, usr.getLastName());
			_ps.setInt(2, usr.getID());
			_ps.setInt(3, Applicant.REJECTED);
			return executeIDs();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to extract database ID data from the result set.
	 */
	private List<Integer> executeIDs() throws SQLException {
		Collection<Integer> results = new LinkedHashSet<Integer>();
		ResultSet rs = _ps.executeQuery();
		while (rs.next())
			results.add(new Integer(rs.getInt(1)));
		
		rs.close();
		_ps.close();
		return new ArrayList<Integer>(results);
	}

	/**
	 * Helper method to extract appliacnt data from the result set.
	 */
	private List<Applicant> execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		List<Applicant> results = new ArrayList<Applicant>();
		while (rs.next()) {
			Applicant a = new Applicant(rs.getString(4), rs.getString(5));
			a.setID(rs.getInt(1));
			a.setStatus(rs.getInt(3));
			a.setPilotID(rs.getInt(2)); // Status must be populated first
			a.setEmail(rs.getString(6));
			a.setLocation(rs.getString(7));
			a.setIMHandle(InstantMessage.AIM, rs.getString(8));
			a.setIMHandle(InstantMessage.MSN, rs.getString(9));
			a.setNetworkID(OnlineNetwork.VATSIM, rs.getString(10));
			a.setNetworkID(OnlineNetwork.IVAO, rs.getString(11));
			a.setLegacyHours(rs.getDouble(12));
			a.setLegacyURL(rs.getString(13));
			a.setLegacyVerified(rs.getBoolean(14));
			a.setHomeAirport(rs.getString(15));
			a.setEquipmentType(rs.getString(16));
			a.setRank(rs.getString(17));
			a.setNotifyOption(Person.FLEET, rs.getBoolean(18));
			a.setNotifyOption(Person.EVENT, rs.getBoolean(19));
			a.setNotifyOption(Person.NEWS, rs.getBoolean(20));
			a.setNotifyOption(Person.PIREP, rs.getBoolean(21));
			a.setEmailAccess(rs.getInt(22));
			a.setCreatedOn(rs.getTimestamp(23));
			// skip 24
			a.setRegisterHostName(rs.getString(25));
			a.setDateFormat(rs.getString(26));
			a.setTimeFormat(rs.getString(27));
			a.setNumberFormat(rs.getString(28));
			a.setAirportCodeType(rs.getInt(29));
			a.setDistanceType(rs.getInt(30));
			a.setSimVersion(rs.getInt(31));
			a.setTZ(TZInfo.get(rs.getString(32)));
			a.setUIScheme(rs.getString(33));
			a.setComments(rs.getString(34));
			a.setHRComments(rs.getString(35));
			a.setRegisterAddress(rs.getString(36));

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