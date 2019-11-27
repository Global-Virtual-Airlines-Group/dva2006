// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.RouteStats;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Flight Reports.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetFlightReports extends DAO {

	private static final Logger log = Logger.getLogger(GetFlightReports.class);

	/**
	 * Initializes the DAO with a given JDBC connection.
	 * @param c the JDBC connection to use
	 */
	public GetFlightReports(Connection c) {
		super(c);
	}

	/**
	 * Returns a PIREP with a particular database ID.
	 * @param id the database ID
	 * @return the Flight Report, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FlightReport get(int id) throws DAOException {
		return get(id, SystemData.get("airline.db"));
	}

	/**
	 * Returns a PIREP with a particular database ID.
	 * @param id the database ID
	 * @param dbName the database Name
	 * @return the Flight Report, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FlightReport get(int id, String dbName) throws DAOException {

		// Build the SQL statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, PC.REMARKS, APR.*, AO.ONTIME FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS PR LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".ACARS_PIREPS APR ON (PR.ID=APR.ID) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".ACARS_ONTIME AO ON (PR.ID=AO.ID) WHERE (PR.ID=?) LIMIT 1");

		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, id);

			// Execute the query, if nothing returned then give back null, otherwise load primary eq types and route
			FlightReport fr = execute(ps).stream().findFirst().orElse(null);
			if (fr != null) {
				fr.setCaptEQType(getCaptEQType(fr.getID(), dbName));
				fr.setRoute(getRoute(fr.getID(), dbName));
			}

			return fr;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns an ACARS-logged PIREP with a particular ACARS Flight ID.
	 * @param dbName the database Name
	 * @param acarsID the ACARS flight ID
	 * @return the ACARSFlightReport, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FDRFlightReport getACARS(String dbName, int acarsID) throws DAOException {

		// Build the SQL statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, PC.REMARKS, APR.*, AO.ONTIME FROM (");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS PR, ");
		sqlBuf.append(db);
		sqlBuf.append(".ACARS_PIREPS APR) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".ACARS_ONTIME AO ON (PR.ID=AO.ID) WHERE (APR.ID=PR.ID) AND (APR.ACARS_ID=?) LIMIT 1");

		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, acarsID);

			// Execute the query, if nothing returned then give back null
			FlightReport fr = execute(ps).stream().findFirst().orElse(null);

			// Check that it's really an ACARSFlightReport object
			if (!(fr instanceof FDRFlightReport))
				return null;

			// Get the primary equipment types
			FDRFlightReport afr = (FDRFlightReport) fr;
			afr.setCaptEQType(getCaptEQType(afr.getID(), dbName));
			afr.setRoute(getRoute(afr.getID(), dbName));
			return afr;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Reports in particular statuses.
	 * @param status a Collection of Integer status codes
	 * @return a List of FlightReports in the specified statuses
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getByStatus(Collection<FlightStatus> status) throws DAOException {
		return getByStatus(status, null);
	}

	/**
	 * Returns all Flight Reports in particular statuses.
	 * @param status a Collection of Integer status codes
	 * @param orderBy the ORDER BY SQL statement
	 * @return a List of FlightReports in the specified statuses
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getByStatus(Collection<FlightStatus> status, String orderBy) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, PC.REMARKS, APR.*, AO.ONTIME, GROUP_CONCAT(ER.EQTYPE) FROM PILOTS P, PIREPS PR LEFT JOIN PIREP_COMMENT PC ON "
			+ "(PR.ID=PC.ID) LEFT JOIN ACARS_PIREPS APR ON (PR.ID=APR.ID) LEFT JOIN ACARS_ONTIME AO ON (PR.ID=AO.ID) LEFT JOIN EQRATINGS ER ON ((ER.RATED_EQ=PR.EQTYPE) AND (ER.RATING_TYPE=?)) "
			+ "WHERE (P.ID=PR.PILOT_ID) AND (");
		for (Iterator<FlightStatus> i = status.iterator(); i.hasNext();) {
			FlightStatus st = i.next();
			sqlBuf.append("(PR.STATUS=");
			sqlBuf.append(st.ordinal());
			sqlBuf.append(')');
			if (i.hasNext())
				sqlBuf.append(" OR ");
		}

		sqlBuf.append(") GROUP BY PR.ID ORDER BY ");
		sqlBuf.append(StringUtils.isEmpty(orderBy) ? "PR.DATE, PR.SUBMITTED, PR.ID" : orderBy);
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, EquipmentType.Rating.PRIMARY.ordinal());
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the number of Flight Reports awaiting disposition.
	 * @param eqType the equipment type, or null if all
	 * @param includeAcademy TRUE if Flight Academy check rides should be counted, otherwise FALSE
	 * @return the number Check Ride Flight Reports in SUBMITTED status
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getCheckRideQueueSize(String eqType, boolean includeAcademy) throws DAOException {
		
		// Build the SQL Statement
		StringBuilder sqlBuf = new StringBuilder("SELECT COUNT(F.ID) FROM PIREPS F, EQRATINGS R WHERE (F.EQTYPE=R.RATED_EQ) AND (R.RATING_TYPE=?) AND (F.STATUS=?) AND ((F.ATTR & ?) > 0)");
		if (!includeAcademy)
			sqlBuf.append(" AND ((F.ATTR & ?) = 0)");
		if (eqType != null)
			sqlBuf.append(" AND (R.EQTYPE=?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			int param = 0;
			ps.setInt(++param, EquipmentType.Rating.PRIMARY.ordinal());
			ps.setInt(++param, FlightStatus.SUBMITTED.ordinal());
			ps.setInt(++param, FlightReport.ATTR_CHECKRIDE);
			if (!includeAcademy)
				ps.setInt(++param, FlightReport.ATTR_ACADEMY);
			if (eqType != null)
				ps.setString(++param, eqType);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the number of held Flight Reports for a particular Pilot.
	 * @param pilotID the Pilot's database ID
	 * @param dbName the database name
	 * @return the number of Flight Reports in HOLD status
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getHeld(int pilotID, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder  buf = new StringBuilder("SELECT COUNT(*) FROM ");
		buf.append(formatDBName(dbName));
		buf.append(".PIREPS WHERE (STATUS=?) AND (PILOT_ID=?)");
		
		try (PreparedStatement ps = prepare(buf.toString())) {
			ps.setInt(1, FlightStatus.HOLD.ordinal());
			ps.setInt(2, pilotID);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Flight Reports associated with a particular Flight Assignment.
	 * @param id the Flight Assignment database ID
	 * @param dbName the database name
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getByAssignment(int id, String dbName) throws DAOException {

		// Build the SQL statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, PC.REMARKS, APR.* FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS PR LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".ACARS_PIREPS APR ON (PR.ID=APR.ID) WHERE (PR.ASSIGN_ID=?)");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, id);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Flight Reports associated with a particular Online Event.
	 * @param id the Online Event database ID
	 * @param dbName the database name
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getByEvent(int id, String dbName) throws DAOException {

		// Build the SQL statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, PC.REMARKS, APR.* FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS PR LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".ACARS_PIREPS APR ON (PR.ID=APR.ID) WHERE (PR.EVENT_ID=?)");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, id);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all Flight Reports for a Pilot for a particular date range.
	 * @param id the Pilot database ID
	 * @param startDate the start date
	 * @param days the number of days forward to include
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getLogbookCalendar(int id, java.time.Instant startDate, int days) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT PR.*, PC.COMMENTS, PC.REMARKS, APR.* FROM PIREPS PR LEFT JOIN PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ACARS_PIREPS APR ON (PR.ID=APR.ID) "
				+ "WHERE (PR.PILOT_ID=?) AND (PR.DATE >= ?) AND (PR.DATE < DATE_ADD(?, INTERVAL ? DAY)) ORDER BY PR.DATE, PR.ID")) {
			ps.setInt(1, id);
			ps.setTimestamp(2, createTimestamp(startDate));
			ps.setTimestamp(3, createTimestamp(startDate));
			ps.setInt(4, days);
			return execute(ps);
		} catch (SQLException se) { 
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Reports for a particular Pilot, using a sort column.
	 * @param id the Pilot database ID
	 * @param criteria the search criteria
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getByPilot(int id, ScheduleSearchCriteria criteria) throws DAOException {
		String db = (criteria == null) ? null : criteria.getDBName();
		return getByPilot(id, criteria, StringUtils.isEmpty(db) ? SystemData.get("airline.db") : db);
	}

	/**
	 * Returns all Flight Reports for a particular Pilot, using a sort column.
	 * @param id the Pilot database ID
	 * @param criteria the search criteria
	 * @param dbName the database name
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getByPilot(int id, ScheduleSearchCriteria criteria, String dbName) throws DAOException {

		// Build the statement
		String db = formatDBName(dbName);
		StringBuilder buf = new StringBuilder("SELECT PR.*, PC.COMMENTS, PC.REMARKS, APR.* FROM ");
		buf.append(db);
		buf.append(".PIREPS PR LEFT JOIN ");
		buf.append(db);
		buf.append(".PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ");
		buf.append(db);
		buf.append(".ACARS_PIREPS APR ON (PR.ID=APR.ID) WHERE (PR.PILOT_ID=?)");
		if (criteria != null) {
			if (criteria.getEquipmentType() != null)
				buf.append(" AND (PR.EQTYPE=?)");
			if (criteria.getAirportD() != null)
				buf.append(" AND (PR.AIRPORT_D=?)");
			if (criteria.getAirportA() != null)
				buf.append(" AND (PR.AIRPORT_A=?)");
			if (criteria.getSortBy() != null) {
				buf.append(" ORDER BY IF(PR.STATUS=?,0,1), PR.");
				buf.append(criteria.getSortBy());
			}
		}
		
		int idx = 1;
		try (PreparedStatement ps = prepare(buf.toString())) {
			ps.setInt(1, id);
			if (criteria != null) {
				if (criteria.getEquipmentType() != null)
					ps.setString(++idx, criteria.getEquipmentType());
				if (criteria.getAirportD() != null)
					ps.setString(++idx, criteria.getAirportD().getIATA());
				if (criteria.getAirportA() != null)
					ps.setString(++idx, criteria.getAirportA().getIATA());
				ps.setInt(++idx, FlightStatus.DRAFT.ordinal());
			}
			
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Gets online and ACARS legs/hours for a particular Pilot.
	 * @param p the Pilot to query for (the object will be updated)
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 * @see GetFlightReports#getOnlineTotals(Map, String)
	 */
	public void getOnlineTotals(Pilot p, String dbName) throws DAOException {
		Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>(2);
		pilots.put(Integer.valueOf(p.getID()), p);
		getOnlineTotals(pilots, dbName);
	}

	/**
	 * Returns online and ACARS legs/hours for a group of Pilots.
	 * @param pilots a Map of Pilot objects to populate with results
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void getOnlineTotals(Map<Integer, Pilot> pilots, String dbName) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT FR.PILOT_ID, COUNT(IF((FR.ATTR & ?) != 0, FR.FLIGHT_TIME, NULL)) AS OC, ROUND(SUM(IF((FR.ATTR & ?) != 0, FR.FLIGHT_TIME, 0)), 1) AS OH, "
			+ "COUNT(IF((FR.ATTR & ?) != 0, FR.FLIGHT_TIME, NULL)) AS AC, ROUND(SUM(IF((FR.ATTR & ?) != 0, FR.FLIGHT_TIME, 0)), 1) AS AH, COUNT(IF(FR.EVENT_ID > 0, FR.ID, NULL)) AS EC, "
			+ "ROUND(SUM(IF(FR.EVENT_ID > 0, FR.FLIGHT_TIME, 0)), 1) AS EH FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PIREPS FR WHERE (FR.STATUS=?) AND FR.PILOT_ID IN (");

		// Append the Pilot IDs
		int setSize = 0;
		for (Pilot p : pilots.values()) {
			if ((p.getACARSLegs() < 0) || (p.getOnlineLegs() < 0)) {
				setSize++;
				sqlBuf.append(String.valueOf(p.getID()));
				sqlBuf.append(',');
			}
		}

		// If we are not querying any pilots, abort
		if (log.isDebugEnabled())
			log.debug("Uncached set size = " + setSize);
		if (setSize == 0)
			return;

		// Strip out trailing comma
		if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
			sqlBuf.setLength(sqlBuf.length() - 1);

		// Close the SQL statement
		sqlBuf.append(") GROUP BY FR.PILOT_ID LIMIT ");
		sqlBuf.append(String.valueOf(setSize));

		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, FlightReport.ATTR_ONLINE_MASK);
			ps.setInt(2, FlightReport.ATTR_ONLINE_MASK);
			ps.setInt(3, FlightReport.ATTR_ACARS);
			ps.setInt(4, FlightReport.ATTR_ACARS);
			ps.setInt(5, FlightStatus.OK.ordinal());

			// Execute the query
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Pilot p = pilots.get(Integer.valueOf(rs.getInt(1)));
					if (p == null) continue;
					
					p.setOnlineLegs(rs.getInt(2));
					p.setOnlineHours(rs.getDouble(3));
					p.setACARSLegs(rs.getInt(4));
					p.setACARSHours(rs.getDouble(5));
					p.setEventLegs(rs.getInt(6));
					p.setEventHours(rs.getDouble(7));
				}
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Set to zero
		pilots.values().forEach(p -> { p.setACARSLegs(Math.max(0, p.getACARSLegs())); p.setOnlineLegs(Math.max(0, p.getOnlineLegs())); });
	}

	/**
	 * Returns Draft Flight Reports for a particular Pilot (with optional city pair).
	 * @param pilotID the Pilot's Database ID
	 * @param rp the RoutePair, or null if none
	 * @param dbName the database Name
	 * @return a List of Draft FlightReports matching the above criteria
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getDraftReports(int pilotID, RoutePair rp, String dbName) throws DAOException {

		// Build the prepared statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, PC.REMARKS, S.TIME_D, S.TIME_A, PRT.ROUTE FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS PR LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREP_ROUTE PRT ON (PR.ID=PRT.ID) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".SCHEDULE S ON (S.AIRLINE=PR.AIRLINE) AND (S.FLIGHT=PR.FLIGHT) AND (S.LEG=PR.LEG) AND (S.AIRPORT_D=PR.AIRPORT_D) AND (S.AIRPORT_A=PR.AIRPORT_A) WHERE (PR.PILOT_ID=?) AND (PR.STATUS=?)");

		// Add departure/arrival airports if specified
		if (rp != null)
			sqlBuf.append(" AND (PR.AIRPORT_D=?) AND (PR.AIRPORT_A=?)");

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, pilotID);
			ps.setInt(2, FlightStatus.DRAFT.ordinal());
			if (rp != null) {
				ps.setString(3, rp.getAirportD().getIATA());
				ps.setString(4, rp.getAirportA().getIATA());
			}

			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves held/submitted Flight diversions for a particular Pilot.
	 * @param a the Airport diverted to
	 * @param pilotID the Pilot's database ID
	 * @param db the database
	 * @return a FlightReport, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FlightReport getDiversion(Airport a, int pilotID, String db) throws DAOException {
		
		// Build the SQL statement
		String dbName = formatDBName(db);
		StringBuilder sqlBuf = new StringBuilder("SELECT PR.*, PC.COMMENTS, PC.REMARKS, APR.* FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".PIREPS PR LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".ACARS_PIREPS APR ON (PR.ID=APR.ID) WHERE (PR.PILOT_ID=?) AND (PR.AIRPORT_A=?) AND ((PR.STATUS=?) OR (PR.STATUS=?)) AND ((PR.ATTR & ?) > 0)");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, pilotID);
			ps.setString(2, a.getIATA());
			ps.setInt(3, FlightStatus.HOLD.ordinal());
			ps.setInt(4, FlightStatus.SUBMITTED.ordinal());
			ps.setInt(5, FlightReport.ATTR_DIVERT);
			return execute(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the city pairs flown by a particular Pilot.
	 * @param pilotID the Pilot database ID
	 * @param days the number of days back to search
	 * @return a Collection of RoutePair beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<RouteStats> getRoutePairs(int pilotID, int days) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT AIRPORT_D, AIRPORT_A, COUNT(ID) AS CNT FROM PIREPS WHERE (PILOT_ID=?) AND (STATUS=?) ");
		if (days > 0)
			sqlBuf.append("AND (DATE>DATE_SUB(CURDATE(), INTERVAL ? DAY)) ");
		sqlBuf.append("GROUP BY AIRPORT_D, AIRPORT_A");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, pilotID);
			ps.setInt(2, FlightStatus.OK.ordinal());
			if (days > 0)
				ps.setInt(3, days);
			
			// Execute the query
			Map<String, RouteStats> results = new HashMap<String, RouteStats>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					RouteStats rp = new RouteStats(SystemData.getAirport(rs.getString(1)),
						SystemData.getAirport(rs.getString(2)), rs.getInt(3));
					RouteStats rp2 = results.get(rp.toString());
					if (rp2 != null)
						rp.add(rp.getFlights());
					else
						results.put(rp.toString(), rp);
				}
			}
			
			return results.values();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to load PIREP data.
	 * @param ps a PreparedStatement
	 * @return a List of FlightReport beans
	 * @throws SQLException if an error occurs
	 */
	protected static List<FlightReport> execute(PreparedStatement ps) throws SQLException {
		List<FlightReport> results = new ArrayList<FlightReport>();
		try (ResultSet rs = ps.executeQuery()) {
			ResultSetMetaData md = rs.getMetaData();
			boolean hasACARS = (md.getColumnCount() > 66);
			boolean hasComments = (md.getColumnCount() > 23);
			boolean hasSchedTimes = (!hasACARS && (md.getColumnCount() > 25));
			boolean hasDraftRoute = (hasSchedTimes && (md.getColumnCount() > 26));
			boolean hasOnTime = (md.getColumnCount() > 71);

			// Iterate throught the results
			while (rs.next()) {
				FlightStatus status = FlightStatus.values()[rs.getInt(4)];
				int attr = rs.getInt(13);
				boolean isACARS = (hasACARS && (rs.getInt(26) != 0));
				boolean isXACARS = isACARS && ((attr & FlightReport.ATTR_XACARS) != 0);
				boolean isSimFDR = isACARS && ((attr & FlightReport.ATTR_SIMFDR) != 0);
				boolean isDraft = (hasSchedTimes && (status == FlightStatus.DRAFT));

				// Build the PIREP as a standard one, or an ACARS pirep
				Airline a = SystemData.getAirline(rs.getString(6));
				int flight = rs.getInt(7); int leg = rs.getInt(8);
				FlightReport p = null;
				if (isSimFDR)
					p = new SimFDRFlightReport(a, flight, leg);
				else if (isXACARS)
					p = new XACARSFlightReport(a, flight, leg);
				else if (isACARS)
					p = new ACARSFlightReport(a, flight, leg);
				else if (isDraft)
					p = new DraftFlightReport(a, flight, leg);
				else
					p = new FlightReport(a, flight, leg);

				// Populate the data
				p.setID(rs.getInt(1));
				p.setDatabaseID(DatabaseID.PILOT, rs.getInt(2));
				p.setRank(Rank.fromName(rs.getString(3)));
				p.setStatus(status);
				p.setDate(expandDate(rs.getDate(5)));
				p.setAirportD(SystemData.getAirport(rs.getString(9)));
				p.setAirportA(SystemData.getAirport(rs.getString(10)));
				p.setEquipmentType(rs.getString(11));
				p.setSimulator(Simulator.fromVersion(rs.getInt(12), Simulator.UNKNOWN));
				p.setAttributes(attr);
				// Skip column #14 - we calculate this in the flight report
				p.setLength(Math.round(rs.getFloat(15) * 10));
				p.setDatabaseID(DatabaseID.DISPOSAL, rs.getInt(16));
				p.setSubmittedOn(toInstant(rs.getTimestamp(17)));
				p.setDisposedOn(toInstant(rs.getTimestamp(18)));
				p.setDatabaseID(DatabaseID.EVENT, rs.getInt(19));
				p.setDatabaseID(DatabaseID.ASSIGN, rs.getInt(20));
				p.setPassengers(rs.getInt(21));
				p.setLoadFactor(rs.getDouble(22));
				if (hasComments) {
					p.setComments(rs.getString(23));
					p.setRemarks(rs.getString(24));
				}
			
				// Load scheduled times
				if (isDraft) {
					DraftFlightReport dp = (DraftFlightReport) p;
					Timestamp dts = rs.getTimestamp(25);
					if (dts != null) {	
						dp.setTimeD(dts.toLocalDateTime());
						dp.setTimeA(rs.getTimestamp(26).toLocalDateTime());
					}
				
					if (hasDraftRoute)
						dp.setRoute(rs.getString(27));
				}

				// Load generic ACARS pirep data
				if (isACARS || isXACARS) {
					FDRFlightReport ap = (FDRFlightReport) p;
					ap.setAttribute(FlightReport.ATTR_ACARS, true);
					ap.setDatabaseID(DatabaseID.ACARS, rs.getInt(26));
					ap.setStartTime(toInstant(rs.getTimestamp(27)));
					ap.setTaxiTime(toInstant(rs.getTimestamp(28)));
					ap.setTaxiWeight(rs.getInt(29));
					ap.setTaxiFuel(rs.getInt(30));
					ap.setTakeoffTime(toInstant(rs.getTimestamp(31)));
					ap.setTakeoffDistance(rs.getInt(32));
					ap.setTakeoffSpeed(rs.getInt(33));
					ap.setTakeoffN1(rs.getDouble(34));
					ap.setTakeoffHeading(rs.getInt(35));
					ap.setTakeoffLocation(new GeoPosition(rs.getDouble(36), rs.getDouble(37), rs.getInt(38)));
					ap.setTakeoffWeight(rs.getInt(39));
					ap.setTakeoffFuel(rs.getInt(40));
					ap.setLandingTime(toInstant(rs.getTimestamp(41)));
					ap.setLandingDistance(rs.getInt(42));
					ap.setLandingSpeed(rs.getInt(43));
					ap.setLandingVSpeed(rs.getInt(44));
					// Load column #45 with DVA ACARS only
					ap.setLandingN1(rs.getDouble(46));
					ap.setLandingHeading(rs.getInt(47));
					ap.setLandingLocation(new GeoPosition(rs.getDouble(48), rs.getDouble(49), rs.getInt(50)));
					ap.setLandingWeight(rs.getInt(51));
					ap.setLandingFuel(rs.getInt(52));
					// Load column #53 with DVA ACARS only
					ap.setEndTime(toInstant(rs.getTimestamp(54)));
					ap.setGateWeight(rs.getInt(55));
					ap.setGateFuel(rs.getInt(56));
					ap.setTotalFuel(rs.getInt(57));
				}
			
				// Load DVA ACARS pirep data
				if (isACARS && !isXACARS) {
					ACARSFlightReport ap = (ACARSFlightReport) p;
					ap.setLandingG(rs.getDouble(45));
					ap.setLandingCategory(ILSCategory.values()[rs.getInt(53)]);
					ap.setPaxWeight(rs.getInt(58));
					ap.setCargoWeight(rs.getInt(59));
					ap.setTime(0, rs.getInt(60));
					ap.setTime(1, rs.getInt(61));
					ap.setTime(2, rs.getInt(62));
					ap.setTime(4, rs.getInt(63));
					ap.setFDE(rs.getString(64));
					ap.setAircraftCode(rs.getString(65));
					ap.setSDK(rs.getString(66));
					ap.setCapabilities(rs.getLong(67));
					ap.setHasReload(rs.getBoolean(68));
					ap.setAverageFrameRate(rs.getInt(69) / 10d);
					ap.setClientBuild(rs.getInt(70));
					ap.setBeta(rs.getInt(71));
					if (hasOnTime)
						ap.setOnTime(OnTime.values()[rs.getInt(72)]);
				} else if (isXACARS) {
					XACARSFlightReport ap = (XACARSFlightReport) p;
					ap.setMajorVersion(rs.getInt(70));
					ap.setMinorVersion(rs.getInt(71));
				}

				results.add(p);
			}
		}

		return results;
	}

	/**
	 * Populates the Equipment Types a collection of PIREPs counts towards promotion in.
	 * @param pireps a Collection of FlightReport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void getCaptEQType(Collection<FlightReport> pireps) throws DAOException {
		if (pireps.isEmpty()) return;

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, EQTYPE FROM PROMO_EQ WHERE (ID IN (");
		for (Iterator<FlightReport> i = pireps.iterator(); i.hasNext();) {
			FlightReport fr = i.next();
			sqlBuf.append(String.valueOf(fr.getID()));
			if (i.hasNext())
				sqlBuf.append(',');
		}

		sqlBuf.append("))");

		// Convert PIREPs to a Map for lookup
		Map<Integer, FlightReport> pMap = CollectionUtils.createMap(pireps, FlightReport::getID);
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					FlightReport fr = pMap.get(Integer.valueOf(rs.getInt(1)));
					if (fr != null)
						fr.setCaptEQType(rs.getString(2));
				}
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to route data.
	 */
	private String getRoute(int id, String dbName) throws SQLException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ROUTE FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PIREP_ROUTE WHERE (ID=?) LIMIT 1");
		String rt = null;
		
		// Build the prepared statement and execute the query
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					rt = rs.getString(1);
			}
		}
		
		return rt;
	}
	
	/*
	 * Helper method to load data for flights counting towards promotion.
	 */
	private Collection<String> getCaptEQType(int id, String dbName) throws SQLException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT EQTYPE FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PROMO_EQ WHERE (ID=?)");

		// Build the prepared statement and execute the query
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, id);
			Collection<String> results = new TreeSet<String>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}
			
			return results;
		}
	}
}