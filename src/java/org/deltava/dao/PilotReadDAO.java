// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2016, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.Restriction;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.stats.DatedAccomplishmentID;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A DAO to support reading Pilot object(s) from the database. This class contains methods to read an individual Pilot
 * from the database; implementing subclasses typically add methods to retrieve Lists of pilots based on particular criteria.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

abstract class PilotReadDAO extends DAO {

	private static final Logger log = Logger.getLogger(PilotReadDAO.class);
	protected static final Cache<Pilot> _cache = CacheManager.get(Pilot.class, "Pilots");

	/**
	 * Creates the DAO from a JDBC connection.
	 * @param c the JDBC connection to use
	 */
	protected PilotReadDAO(Connection c) {
		super(c);
	}

	/**
	 * Gets a pilot object based on a database ID. <i>This uses a cached query, and populates ratings and roles. </i>
	 * @param id the database ID of the Pilot object
	 * @return the Pilot object, or null if the ID was not found
	 * @throws DAOException if a JDBC error occured
	 */
	public final Pilot get(int id) throws DAOException {

		// Check if we're in the cache
		Pilot p = _cache.get(Integer.valueOf(id));
		if (p != null)
			return p;

		try (PreparedStatement ps = prepare("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE), S.EXT, S.MODIFIED FROM PILOTS P LEFT JOIN PIREPS F "
				+ "ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN SIGNATURES S ON (P.ID=S.ID) WHERE (P.ID=?) GROUP BY P.ID LIMIT 1")) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setInt(2, id);

			// Execute the query and get the result
			p = execute(ps).stream().findFirst().orElse(null);
			if (p == null) return null;

			// Add roles/ratings
			loadChildRows(p, SystemData.get("airline.db"));
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		_cache.add(p);
		return p;
	}

	/**
	 * Returns a Pilot based on a given full name. This method does not use first/last name splitting since this can be
	 * unpredictable.
	 * @param fullName the Full Name of the Pilot
	 * @param dbName the database name to search
	 * @return a Collection of Pilot beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public final List<Pilot> getByName(String fullName, String dbName) throws DAOException {

		// Build the SQL statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE), S.EXT, S.MODIFIED FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".PILOTS P LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".SIGNATURES S ON (P.ID=S.ID) WHERE (CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME)=?) GROUP BY P.ID");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setString(2, fullName);

			// Execute the query and get the result
			Map<Integer, Pilot> results = CollectionUtils.createMap(execute(ps), Pilot::getID);
			loadIMAddrs(results, dbName);
			loadRatings(results, dbName);
			loadRoles(results);
			loadAccomplishments(results, dbName);

			// Add the result to the cache and return
			_cache.addAll(results.values());
			return new ArrayList<Pilot>(results.values());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns a Pilot object which may be in another Airline's database.
	 * @param ud the UserData bean containg the Pilot location
	 * @return the Pilot bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Pilot get(UserData ud) throws DAOException {
		if (ud == null) return null;

		// Get a map from the table, and get the first value
		Map<Integer, Pilot> pilots = getByID(Collections.singleton(ud), ud.getDB() + "." + ud.getTable());
		Map.Entry<Integer, Pilot> me = pilots.entrySet().stream().findFirst().orElse(null);
		return (me == null) ? null : me.getValue();
	}
	
	/**
	 * Returns Pilot objects which may be in another Airline's database.
	 * @param udm the UserDataMap bean containg the Pilot locations
	 * @return a Map of Pilots indexed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Pilot> get(UserDataMap udm) throws DAOException {
		Map<Integer, Pilot> results = new HashMap<Integer, Pilot>();
		for (String tableName : udm.getTableNames()) {
			if (UserData.isPilotTable(tableName))
				results.putAll(getByID(udm.getByTable(tableName), tableName));
		}
		
		return results;
	}

	/**
	 * Returns a Map of pilots based on a Set of pilot IDs. This is typically called by a Water Cooler thread/channel list command.
	 * @param ids a Collection of pilot IDs. This can either be a Collection of Integers, a Collection of {@link DatabaseBean}beans
	 * @param tableName the table to read from, in <i>DATABASE.TABLE </i> format for a remote database, or <i>TABLE </i>
	 * for a table in the current airline's database.
	 * @return a Map of Pilots, indexed by the pilot code
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Pilot> getByID(Collection<?> ids, String tableName) throws DAOException {

		// Get the datbaase - if we haven't specified one, use the current database
		int ofs = tableName.indexOf('.');
		String dbName = (ofs == -1) ? SystemData.get("airline.db").toLowerCase() : formatDBName(tableName);
		String table = (ofs != -1) ? tableName.substring(ofs + 1) : tableName;

		List<Pilot> results = new ArrayList<Pilot>(ids.size());
		if (log.isDebugEnabled())
			log.debug("Raw set size = " + ids.size());

		// Init the prepared statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE), S.EXT, S.MODIFIED FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append('.');
		sqlBuf.append(table);
		sqlBuf.append(" P LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN ");
		sqlBuf.append(dbName);
		sqlBuf.append(".SIGNATURES S ON (P.ID=S.ID) WHERE (P.ID IN (");

		// Add the pilot IDs to the set
		int querySize = 0;
		for (Iterator<?> i = ids.iterator(); i.hasNext();) {
			Object rawID = i.next();
			Integer id = (rawID instanceof Integer) ? (Integer) rawID : Integer.valueOf(((DatabaseBean) rawID).getID());

			// Pull from the cache if at all possible; this is an evil query
			Pilot p = _cache.get(id);
			if (p != null) {
				results.add(p);
			} else {
				querySize++;
				sqlBuf.append(id.toString());
				if (i.hasNext())
					sqlBuf.append(',');
			}
		}

		// Only execute the prepared statement if we haven't gotten anything from the cache
		if (querySize > 0) {
			if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
				sqlBuf.setLength(sqlBuf.length() - 1);

			sqlBuf.append(")) GROUP BY P.ID");
			List<Pilot> uncached = null;
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				ps.setInt(1, FlightStatus.OK.ordinal());
				uncached = execute(ps);

				// Convert to a map and load ratings/roles
				Map<Integer, Pilot> ucMap = CollectionUtils.createMap(uncached, Pilot::getID);
				loadIMAddrs(ucMap, dbName);
				loadRatings(ucMap, dbName);
				loadRoles(ucMap);
				loadAccomplishments(ucMap, dbName);
			} catch (SQLException se) {
				log.error("Query = " + sqlBuf.toString());
				throw new DAOException(se);
			}

			// If the database does not equal the current airline code, refresh all of the Pilot IDs with the pilot number, but use the database name as the airline code.
			updatePilotCodes(uncached, dbName);

			// Add to results and the cache
			results.addAll(uncached);
			_cache.addAll(uncached);
		}

		// Convert to a Map for easy searching
		return CollectionUtils.createMap(results, Pilot::getID);
	}
	
	/**
	 * Query pilot objects from the database, assuming a pre-prepared statement.
	 * @param ps a PreparedStatement
	 * @return a List of Pilot objects
	 * @throws SQLException if a JDBC error occurs
	 */
	protected static final List<Pilot> execute(PreparedStatement ps) throws SQLException {
		String airlineCode = SystemData.get("airline.code");
		
		List<Pilot> results = new ArrayList<Pilot>();
		try (ResultSet rs = ps.executeQuery()) {
			int columnCount = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				Pilot p = new Pilot(rs.getString(3), rs.getString(4));
				p.setID(rs.getInt(1));
				p.setPilotCode(airlineCode + String.valueOf(rs.getInt(2)));
				p.setStatus(PilotStatus.values()[rs.getInt(5)]);
				p.setDN(rs.getString(6));
				p.setEmail(rs.getString(7));
				p.setLocation(rs.getString(8));
				p.setLegacyHours(rs.getDouble(9));
				p.setHomeAirport(rs.getString(10));
				p.setEquipmentType(rs.getString(11));
				p.setRank(Rank.values()[rs.getInt(12)]);
				p.setNetworkID(OnlineNetwork.VATSIM, rs.getString(13));
				p.setNetworkID(OnlineNetwork.IVAO, rs.getString(14));
				p.setNetworkID(OnlineNetwork.PILOTEDGE, rs.getString(15));
				p.setCreatedOn(toInstant(rs.getTimestamp(16)));
				p.setLoginCount(rs.getInt(17));
				p.setLastLogin(toInstant(rs.getTimestamp(18)));
				p.setLastLogoff(toInstant(rs.getTimestamp(19)));
				p.setTZ(TZInfo.get(rs.getString(20)));
				p.setNotificationCode(rs.getInt(21));
				p.setEmailAccess(rs.getInt(22));
				p.setShowSignatures(rs.getBoolean(23));
				p.setShowSSThreads(rs.getBoolean(24));
				p.setHasDefaultSignature(rs.getBoolean(25));
				p.setShowNewPosts(rs.getBoolean(26));
				p.setIsPermanent(rs.getBoolean(27));
				p.setIsForgotten(rs.getBoolean(28));
				p.setProficiencyCheckRides(rs.getBoolean(29));
				p.setUIScheme(rs.getString(30));
				p.setShowNavBar(rs.getBoolean(31));
				p.setViewCount(rs.getInt(32));
				p.setLoginHost(rs.getString(33));
				p.setDateFormat(rs.getString(34));
				p.setTimeFormat(rs.getString(35));
				p.setNumberFormat(rs.getString(36));
				p.setAirportCodeType(Airport.Code.values()[rs.getInt(37)]);
				p.setDistanceType(DistanceUnit.values()[rs.getInt(38)]);
				p.setWeightType(WeightUnit.values()[rs.getInt(39)]);
				p.setMapType(MapType.values()[rs.getInt(40)]);
				p.setNoExams(rs.getBoolean(41));
				p.setNoVoice(rs.getBoolean(42));
				p.setNoCooler(rs.getBoolean(43));
				p.setNoTimeCompression(rs.getBoolean(44));
				p.setACARSRestriction(Restriction.values()[rs.getInt(45)]);
				p.setEmailInvalid(rs.getBoolean(46));
				p.setLDAPName(rs.getString(47));
				p.setMotto(rs.getString(48));

				// Check if this result set has columns 49-52, which is the PIREP totals
				if (columnCount > 51) {
					p.setLegs(rs.getInt(49));
					p.setMiles(rs.getLong(50));
					p.setHours(rs.getDouble(51));
					p.setLastFlight(expandDate(rs.getDate(52)));
				}

				// Check if this result set has columns 53/54, which is the signature data
				if (columnCount > 53) {
					p.setSignatureExtension(rs.getString(53));
					p.setSignatureModified(toInstant(rs.getTimestamp(54)));
				}

				// Check if this result set has columns 55/56, which are online legs/hours
				if (columnCount > 55) {
					p.setOnlineLegs(rs.getInt(55));
					p.setOnlineHours(rs.getDouble(56));
				}

				results.add(p);
			}
		}

		return results;
	}
	
	/**
	 * Load the ratings, roles and accomplishment IDs for a Pilot.
	 * @param p the Pilot bean
	 * @param dbName the database Name
	 * @throws SQLException if a JDBC error occurs
	 */
	protected final void loadChildRows(Pilot p, String dbName) throws SQLException {
		Map<Integer, Pilot> tmpMap = Map.of(Integer.valueOf(p.getID()), p);
		loadIMAddrs(tmpMap, dbName);
		loadRatings(tmpMap, dbName);
		loadRoles(tmpMap);
		loadAccomplishments(tmpMap, dbName);
	}

	/**
	 * Load the accomplishment IDs for a group of Pilots.
	 * @param pilots the Map of Pilots, indexed by database ID
	 * @param dbName the database Name
	 * @throws SQLException if a JDBC error occurs
	 */
	protected final void loadAccomplishments(Map<Integer, Pilot> pilots, String dbName) throws SQLException {
		if (pilots.isEmpty()) return;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT PILOT_ID, AC_ID, DATE FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOT_ACCOMPLISHMENTS WHERE (PILOT_ID IN (");
		sqlBuf.append(StringUtils.listConcat(pilots.keySet(), ","));
		sqlBuf.append("))");

		// Execute the query
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Pilot p = pilots.get(Integer.valueOf(rs.getInt(1)));
					if (p != null)
						p.addAccomplishmentID(new DatedAccomplishmentID(toInstant(rs.getTimestamp(3)), rs.getInt(2)));
				}
			}
		}
	}
	
	/**
	 * Load the security roles for a group of Pilots.
	 * @param pilots the Map of Pilots, indexed by database ID
	 * @throws SQLException if a JDBC error occurs
	 */
	protected final void loadRoles(Map<Integer, Pilot> pilots) throws SQLException {
		if (pilots.isEmpty()) return;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, ROLE FROM common.AUTH_ROLES WHERE (ID IN (");
		sqlBuf.append(StringUtils.listConcat(pilots.keySet(), ","));
		sqlBuf.append("))");

		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Pilot p = pilots.get(Integer.valueOf(rs.getInt(1)));
					if (p != null)
						p.addRole(rs.getString(2));
				}
			}
		}
	}

	/**
	 * Load social media addresses for a group of Pilots.
	 * @param pilots the Map of Pilots, indexed by database ID
	 * @param dbName the database name
	 * @throws SQLException if a JDBC error occurs
	 */
	protected final void loadIMAddrs(Map<Integer, Pilot> pilots, String dbName) throws SQLException {
		if (pilots.isEmpty()) return;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, TYPE, ADDR FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOT_IMADDR WHERE (ID IN (");
		sqlBuf.append(StringUtils.listConcat(pilots.keySet(), ","));
		sqlBuf.append("))");
		
		// Execute the query
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Pilot p = pilots.get(Integer.valueOf(rs.getInt(1)));
					if (p != null) {
						String imType = rs.getString(2);
						try {
							IMAddress addrType = IMAddress.valueOf(imType);
							p.setIMHandle(addrType, rs.getString(3));
						} catch (Exception e) {
							log.warn("Unknown IM address type - " + imType);
						}
					}	
				}
			}
		}
	}
	
	/**
	 * Load the equipment ratings for a group of Pilots.
	 * @param pilots the Map of Pilots, indexed by database ID
	 * @param dbName the database name
	 * @throws SQLException if a JDBC error occurs
	 */
	protected final void loadRatings(Map<Integer, Pilot> pilots, String dbName) throws SQLException {
		if (pilots.isEmpty()) return;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, RATING FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".RATINGS WHERE (ID IN (");
		sqlBuf.append(StringUtils.listConcat(pilots.keySet(), ","));
		sqlBuf.append("))");

		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Pilot p = pilots.get(Integer.valueOf(rs.getInt(1)));
					if (p != null)
						p.addRating(rs.getString(2));
				}
			}
		}
	}
	
	/**
	 * Updates the Pilot codes when loading Pilots from different databases.
	 * @param pilots a Collection of Pilot beans to update
	 * @param dbName the database name
	 */
	protected static final void updatePilotCodes(Collection<Pilot> pilots, String dbName) {
		if (SystemData.get("airline.db").equals(dbName)) return;
		for (AirlineInformation info : SystemData.getApps()) {
			if (dbName.equals(info.getDB())) {
				pilots.forEach(p -> p.setPilotCode(info.getCode() + String.valueOf(p.getPilotNumber())));
				break;
			}
		}
	}
}