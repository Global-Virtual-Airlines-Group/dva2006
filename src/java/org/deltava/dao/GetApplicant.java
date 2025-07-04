// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2016, 2017, 2018, 2019, 2020, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.system.IPBlock;

import org.deltava.util.*;

/**
 * A Data Access Object to read Applicant data.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class GetApplicant extends DAO implements PersonUniquenessDAO {

	private static final Logger log = LogManager.getLogger(GetApplicant.class);
	
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT *, INET6_NTOA(REGADDR) FROM APPLICANTS WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, id);

			// Get results, return first or null
			Applicant a = execute(ps).stream().findFirst().orElse(null);
			if (a != null)
				loadStageChoices(a);
			
			return a;
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
		try (PreparedStatement ps = prepare("SELECT *, INET6_NTOA(REGADDR) FROM APPLICANTS WHERE (STATUS=?) AND (FIRSTNAME LIKE ?) AND (LASTNAME LIKE ?)")) {
			ps.setInt(1, ApplicantStatus.PENDING.ordinal());
			ps.setString(2, fName);
			ps.setString(3, lName);
			return execute(ps);
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT *, INET6_NTOA(REGADDR) FROM APPLICANTS WHERE (PILOT_ID=?) LIMIT 1")) {
			ps.setInt(1, pilotID);
			return execute(ps).stream().findFirst().orElse(null);
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
	public Map<Integer, Applicant> getByID(Collection<?> ids, String tableName) throws DAOException {
		List<Applicant> results = new ArrayList<Applicant>();
		log.debug("Raw set size = {}", Integer.valueOf(ids.size()));

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT A.*, INET6_NTOA(A.REGADDR) FROM ");
		sqlBuf.append(tableName);
		sqlBuf.append(" A WHERE (A.ID IN (");
		int querySize = 0;
		for (Iterator<?> i = ids.iterator(); i.hasNext();) {
			Object rawID = i.next();
			Integer id = (rawID instanceof Integer) ? (Integer) rawID : Integer.valueOf(((UserData) rawID).getID());

			// Pull from the cache if at all possible; this is an evil query
			querySize++;
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}

		// Only execute the prepared statement if we haven't gotten anything from the cache
		if (querySize > 0) {
			if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
				sqlBuf.setLength(sqlBuf.length() - 1);

			sqlBuf.append("))");
			List<Applicant> uncached = null;
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				uncached = execute(ps);
				for (Applicant a : uncached)
					loadStageChoices(a);
			} catch (SQLException se) {
				throw new DAOException(se);
			}

			results.addAll(uncached);
		}

		return CollectionUtils.createMap(results, Applicant::getID);
	}
	
	/**
	 * Returns an Applicant object which may be in another Airline's database.
	 * @param ud the UserData bean containg the Applicant location
	 * @return the Applicant bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Applicant get(UserData ud) throws DAOException {
		if (ud == null) return null;

		// Get a map from the table, and get the first value
		Map<Integer, Applicant> pilots = getByID(Collections.singleton(ud), ud.getDB() + "." + ud.getTable());
		return pilots.values().stream().findFirst().orElse(null);
	}

	/**
	 * Returns Applicant objects which may be in another Airline's database.
	 * @param udm the UserDataMap bean containg the Pilot locations
	 * @return a Map of Pilots indexed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	@Override
	public Map<Integer, Applicant> get(UserDataMap udm) throws DAOException {
		Map<Integer, Applicant> results = new HashMap<Integer, Applicant>();
		for (String tableName : udm.getTableNames()) {
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT *, INET6_NTOA(REGADDR) FROM APPLICANTS WHERE (LDAP_DN=?) LIMIT 1")) {
			ps.setString(1, directoryName);
			return execute(ps).stream().findFirst().orElse(null);
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

		try (PreparedStatement ps = prepare("SELECT *, INET6_NTOA(REGADDR) FROM APPLICANTS WHERE (LEFT(LASTNAME, 1)=?) ORDER BY CREATED DESC")) {
			ps.setString(1, letter.substring(0, 1).toUpperCase());
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Applicants with a particular status.
	 * @param status the ApplicantStatus
	 * @param orderBy the sort column
	 * @return a List of Applicants
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Applicant> getByStatus(ApplicantStatus status, String orderBy) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT *, INET6_NTOA(REGADDR) FROM APPLICANTS WHERE (STATUS=?)");
		if (!StringUtils.isEmpty(orderBy)) {
			sqlBuf.append(" ORDER BY ");
			sqlBuf.append(orderBy);
		}

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, status.ordinal());
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT *, INET6_NTOA(REGADDR) FROM APPLICANTS WHERE (STATUS=?) AND (EQTYPE=?) ORDER BY LASTNAME")) {
			ps.setInt(1, ApplicantStatus.APPROVED.ordinal());
			ps.setString(2, eqType);
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT COUNT(DISTINCT ID) FROM APPLICANTS WHERE (STATUS <> ?) AND (REGADDR=INET6_ATON(?)) AND (CREATED > DATE_SUB(NOW(), INTERVAL ? DAY))")) {
			ps.setInt(1, ApplicantStatus.REJECTED.ordinal());
			ps.setString(2, addr);
			ps.setInt(3, Math.max(1, days));
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && (rs.getInt(1) > 0);
			}
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
	@Override
	public Collection<Integer> checkUnique(Person p, String dbName) throws DAOException {
		return checkUnique(p, dbName, -1);
	}

	/**
	 * Checks if an Applicant is unique, by checking the first/last names and the e-mail address. This will not return a
	 * match against rejected Applicants.
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
		sqlBuf.append(".APPLICANTS WHERE (STATUS=?) AND (((FIRSTNAME=?) AND (LASTNAME=?))");
		if (!StringUtils.isEmpty(p.getEmail()))
			sqlBuf.append(" OR (EMAIL=?)");
		sqlBuf.append(')');
		if (days > 0)
			sqlBuf.append(" AND (CREATED > DATE_SUB(NOW(), INTERVAL ? DAY))");

		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			int param = 0;
			ps.setInt(++param, ApplicantStatus.PENDING.ordinal());
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
	 * Searches for Applicants registering from the same TCP/IP network. If the Applicant has
	 * been hired, this will return the Pilot database ID, instead of the Applicant database ID.
	 * @param addrBlock the network block
	 * @param dbName the database name
	 * @return a Collection of Database IDs as Integers
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> checkAddress(IPBlock addrBlock, String dbName) throws DAOException {
		if (addrBlock == null)
			return Collections.emptyList();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT IF(PILOT_ID, PILOT_ID, ID) FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".APPLICANTS A WHERE (REGADDR >= INET6_ATON(?)) AND (REGADDR <= INET6_ATON(?)) ORDER BY CREATED DESC");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, addrBlock.getAddress());
			ps.setString(2, addrBlock.getLastAddress());
			return executeIDs(ps);
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
	@Override
	public Collection<Integer> checkSoundex(Person usr, String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, SOUNDEX(?) AS TARGET, SOUNDEX(LASTNAME COLLATE utf8mb4_unicode_ci) AS SX FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".APPLICANTS WHERE (ID<>?) AND (STATUS<>?) HAVING ((LEFT(SX, LENGTH(TARGET))=TARGET) OR (LEFT(TARGET, LENGTH(SX))=SX)) ORDER BY ID");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, usr.getLastName());
			ps.setInt(2, usr.getID());
			ps.setInt(3, ApplicantStatus.REJECTED.ordinal());
			return executeIDs(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves Applicants automatically rejected more than a particular number of hours ago.
	 * @param hours the number of hours ago
	 * @return a Collection of Applicants
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Applicant> getAutoRejected(int hours) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT *, INET6_NTOA(REGADDR) FROM APPLICANTS WHERE (STATUS=?) AND (AUTO_REJECT=?) AND (CREATED<DATE_SUB(NOW(), INTERVAL ? HOUR))")) {
			ps.setInt(1, ApplicantStatus.REJECTED.ordinal());
			ps.setBoolean(2, true);
			ps.setInt(3, hours);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to populate stage program choices.
	 */
	private void loadStageChoices(Applicant a) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM APPLICANT_STAGE_CHOICES WHERE (ID=?)")) {
			ps.setInt(1, a.getID());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					a.setTypeChoice(rs.getInt(2), rs.getString(3));
			}
		}
	}

	/*
	 * Helper method to extract appliacnt data from the result set.
	 */
	private static List<Applicant> execute(PreparedStatement ps) throws SQLException {
		List<Applicant> results = new ArrayList<Applicant>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Applicant a = new Applicant(rs.getString(4), rs.getString(5));
				a.setID(rs.getInt(1));
				a.setStatus(ApplicantStatus.values()[rs.getInt(3)]);
				a.setPilotID(rs.getInt(2)); // Status must be populated first
				a.setEmail(rs.getString(6));
				a.setLocation(rs.getString(7));
				//a.setIMHandle(IMAddress.AIM, rs.getString(8)); // AIM Handle
				//a.setIMHandle(IMAddress.MSN, rs.getString(9)); // MSN Handle
				a.setNetworkID(OnlineNetwork.VATSIM, rs.getString(10));
				a.setNetworkID(OnlineNetwork.IVAO, rs.getString(11));
				a.setNetworkID(OnlineNetwork.PILOTEDGE, rs.getString(12));
				a.setNetworkID(OnlineNetwork.POSCON, rs.getString(13));
				a.setLegacyHours(rs.getDouble(14));
				a.setLegacyURL(rs.getString(15));
				a.setLegacyVerified(rs.getBoolean(16));
				a.setHasCAPTCHA(rs.getBoolean(17));
				a.setAutoReject(rs.getBoolean(18));
				a.setHomeAirport(rs.getString(19));
				a.setEquipmentType(rs.getString(20));
				a.setRank(Rank.fromName(rs.getString(21)));
				a.setNotificationCode(rs.getInt(22));
				a.setEmailAccess(rs.getInt(23));
				a.setCreatedOn(toInstant(rs.getTimestamp(24)));
				// skip 25
				a.setRegisterHostName(rs.getString(26));
				a.setDateFormat(rs.getString(27));
				a.setTimeFormat(rs.getString(28));
				a.setNumberFormat(rs.getString(29));
				a.setAirportCodeType(Airport.Code.values()[rs.getInt(30)]);
				a.setDistanceType(DistanceUnit.values()[rs.getInt(31)]);
				a.setWeightType(WeightUnit.values()[rs.getInt(32)]);
				a.setSimVersion(Simulator.values()[rs.getInt(33)]);
				a.setTZ(TZInfo.get(rs.getString(34)));
				a.setUIScheme(rs.getString(35));
				a.setComments(rs.getString(36));
				a.setHRComments(rs.getString(37));
				a.setRegisterAddress(rs.getString(38));
				results.add(a);
			}
		}

		return results;
	}
}