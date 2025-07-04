// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.stats.RouteOnTime;
import org.deltava.beans.simbrief.BriefingPackage;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access object to write Flight Reports to the database.
 * @author Luke
 * @version 11.6
 * @since 1.0
 */

public class SetFlightReport extends DAO {
	
	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetFlightReport(Connection c) {
		super(c);
	}

	/**
	 * Deletes a Flight Report from the database.
	 * @param id the Flight Report database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM PIREPS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes ACARS flight data from the database.
	 * @param id the Flight Report database ID
	 * @return TRUE if a record was deleted, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean deleteACARS(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM ACARS_PIREPS WHERE (ID=?)")) {
			ps.setInt(1, id);
			return (executeUpdate(ps, 0) > 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes Elite program Flight Report data from the database.
	 * @param fr the FlightReport
	 * @return TRUE if a record was deleted, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean deleteElite(FlightReport fr) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM PIREP_ELITE WHERE (ID=?)")) {
			ps.setInt(1, fr.getID());
			return (executeUpdate(ps, 0) > 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			YearlyTotal yt = new YearlyTotal(EliteScorer.getStatsYear(fr.getDate()), fr.getAuthorID());
			CacheManager.invalidate("EliteLifetime", Integer.valueOf(fr.getAuthorID()));
			CacheManager.invalidate("EliteYearlyTotal", yt.cacheKey());
			CacheManager.invalidate("EliteYearlyTotal", Integer.valueOf(fr.getAuthorID()));
		}
	}

	/**
	 * Disposes of a Flight Report, by setting its status to Approved, Rejected or Held.
	 * @param db the database name
	 * @param usr the Person updating the Flight Report
	 * @param pirep the Flight Report
	 * @param status the new FlightStatus
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if pirep is null
	 */
	public void dispose(String db, Person usr, FlightReport pirep, FlightStatus status) throws DAOException {
		String dbName = formatDBName(db);
		try {
			startTransaction();

			// Write the PIREP
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE " + dbName + ".PIREPS SET STATUS=?, ATTR=?, DISPOSAL_ID=?, DISPOSED=NOW() WHERE (ID=?)")) {
				ps.setInt(1, status.ordinal());
				ps.setInt(2, pirep.getAttributes());
				ps.setInt(3, (usr == null) ? 0 : usr.getID());
				ps.setInt(4, pirep.getID());
				executeUpdate(ps, 1);
			}
			
			// Save the promotion equipment types and comments
			writePromoEQ(pirep.getID(), dbName, pirep.getCaptEQType());
			writeComments(pirep, dbName);
			writeHistory(pirep.getStatusUpdates(), dbName);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			Integer pKey = Integer.valueOf(pirep.getDatabaseID(DatabaseID.PILOT));
			CacheManager.invalidate("Pilots", pKey);
			CacheManager.invalidate("Logbook", pKey);
			CacheManager.invalidate("OnTimeRoute", RouteOnTime.createKey(pirep, db));
		}
	}
	
	/**
	 * Marks a Flight Report as widhrdawn.
	 * @param fr the FlightReport
	 * @param db the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void withdraw(FlightReport fr, String db) throws DAOException {
		String dbName = formatDBName(db);
		try (PreparedStatement ps = prepare("UPDATE " + dbName + ".PIREPS SET STATUS=?, DISPOSED=NULL, DISPOSAL_ID=? WHERE (ID=?) AND (STATUS=?)")) {
			ps.setInt(1, FlightStatus.DRAFT.ordinal());
			ps.setInt(2, 0);
			ps.setInt(3, fr.getID());
			ps.setInt(4, FlightStatus.SUBMITTED.ordinal());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Private helper method to prepare an INSERT statement for a new Flight Report.
	 */
	private void insert(FlightReport fr, String db) throws SQLException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("INSERT INTO ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS (PILOT_ID, RANKING, STATUS, DATE, AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, EQTYPE, FSVERSION, ATTR, DISTANCE, FLIGHT_TIME, SUBMITTED, EVENT_ID, ASSIGN_ID, TOUR_ID, PAX, LOADFACTOR, FLIGHT_TYPE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		// Set the prepared statement parameters
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, fr.getDatabaseID(DatabaseID.PILOT));
			ps.setString(2, fr.getRank().getName());
			ps.setInt(3, fr.getStatus().ordinal());
			ps.setTimestamp(4, createTimestamp(fr.getDate()));
			ps.setString(5, fr.getAirline().getCode());
			ps.setInt(6, fr.getFlightNumber());
			ps.setInt(7, fr.getLeg());
			ps.setString(8, fr.getAirportD().getIATA());
			ps.setString(9, fr.getAirportA().getIATA());
			ps.setString(10, fr.getEquipmentType());
			ps.setInt(11, fr.getSimulator().getCode());
			ps.setInt(12, fr.getAttributes());
			ps.setInt(13, fr.getDistance());
			ps.setDouble(14, (fr.getLength() / 10.0));
			ps.setTimestamp(15, createTimestamp(fr.getSubmittedOn()));
			ps.setInt(16, fr.getDatabaseID(DatabaseID.EVENT));
			ps.setInt(17, fr.getDatabaseID(DatabaseID.ASSIGN));
			ps.setInt(18, fr.getDatabaseID(DatabaseID.TOUR));
			ps.setInt(19, fr.getPassengers());
			ps.setDouble(20, fr.getLoadFactor());
			ps.setInt(21, fr.getFlightType().ordinal());
			executeUpdate(ps, 1);
		}
	}

	/*
	 * Private helper method to prepare an UPDATE statement for an existing Flight Report.
	 */
	private void update(FlightReport fr, String db) throws SQLException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS SET STATUS=?, DATE=?, AIRLINE=?, FLIGHT=?, LEG=?, AIRPORT_D=?, AIRPORT_A=?, EQTYPE=?, FSVERSION=?, ATTR=?, DISTANCE=?, FLIGHT_TIME=?, DISPOSAL_ID=?, SUBMITTED=?, "
			+ "DISPOSED=?, ASSIGN_ID=?, EVENT_ID=?, TOUR_ID=?, PAX=?, LOADFACTOR=?, FLIGHT_TYPE=? WHERE (ID=?)");

		// Set the prepared statement parameters
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, fr.getStatus().ordinal());
			ps.setTimestamp(2, createTimestamp(fr.getDate()));
			ps.setString(3, fr.getAirline().getCode());
			ps.setInt(4, fr.getFlightNumber());
			ps.setInt(5, fr.getLeg());
			ps.setString(6, fr.getAirportD().getIATA());
			ps.setString(7, fr.getAirportA().getIATA());
			ps.setString(8, fr.getEquipmentType());
			ps.setInt(9, fr.getSimulator().getCode());
			ps.setInt(10, fr.getAttributes());
			ps.setInt(11, fr.getDistance());
			ps.setDouble(12, (fr.getLength() / 10.0));
			ps.setInt(13, fr.getDatabaseID(DatabaseID.DISPOSAL));
			ps.setTimestamp(14, createTimestamp(fr.getSubmittedOn()));
			ps.setTimestamp(15, createTimestamp(fr.getDisposedOn()));
			ps.setInt(16, fr.getDatabaseID(DatabaseID.ASSIGN));
			ps.setInt(17, fr.getDatabaseID(DatabaseID.EVENT));
			ps.setInt(18, fr.getDatabaseID(DatabaseID.TOUR));
			ps.setInt(19, fr.getPassengers());
			ps.setDouble(20, fr.getLoadFactor());
			ps.setInt(21, fr.getFlightType().ordinal());
			ps.setInt(22, fr.getID());
			executeUpdate(ps, 1);
		}
	}
	
	/*
	 * Helper method to write draft flight reprot fields to the database.
	 */
	private void writeDraft(FlightReport fr, String dbName) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM " + dbName + ".PIREP_DRAFT WHERE (ID=?)")) {
			ps.setInt(1, fr.getID());
			executeUpdate(ps, 0);
		}
		
		if (!(fr instanceof DraftFlightReport dfr)) return;
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO " + dbName + ".PIREP_DRAFT (ID, TIME_D, TIME_A, GATE_D, GATE_A, ALTITUDE) VALUES (?,?,?,?,?,?)")) {
			ps.setInt(1, dfr.getID());
			ps.setTimestamp(2, (dfr.getTimeD() == null) ? null : Timestamp.valueOf(dfr.getTimeD().toLocalDateTime()));
			ps.setTimestamp(3, (dfr.getTimeA() == null) ? null : Timestamp.valueOf(dfr.getTimeA().toLocalDateTime()));
			ps.setString(4, dfr.getGateD());
			ps.setString(5, dfr.getGateA());
			ps.setString(6, dfr.getAltitude());
			executeUpdate(ps, 1);
		}
	}

	/**
	 * Clears whether a Flight Report counts towards promotion to Captain.
	 * @param id the Flight Report database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearPromoEQ(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM PROMO_EQ WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Sets whether a Flight Report counts towards promotion to Captain.
	 * @param id the Flight Report database ID
	 * @param eqTypes
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setPromoEQ(int id, Collection<String> eqTypes) throws DAOException {
		try {
			startTransaction();
			writePromoEQ(id, SystemData.get("airline.db"), eqTypes);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates Flight Report comments.
	 * @param fr the FlightReport bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeComments(FlightReport fr) throws DAOException {
		try {
			writeComments(fr, SystemData.get("airline.db"));
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates a Flight Report's landing score.
	 * @param pirepID the Flight Report database ID
	 * @param score the landing score
	 * @throws DAOException if a JDBC error occurs
	 */
	public void updateLandingScore(int pirepID, double score) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE ACARS_PIREPS SET LANDING_SCORE=? WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, (int)Math.round(score * 100));
			ps.setInt(2, pirepID);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates passenger count for a flight report in the current airline.
	 * @param pirepID the Flight Report database ID
	 * @return TRUE if the passenger count was updated, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean updatePaxCount(int pirepID) throws DAOException {
		AirlineInformation ai = SystemData.getApp(null);
		UserData ud = new UserData(ai.getDB(), "PILOTS", ai.getDomain());
		return updatePaxCount(pirepID, ud);
	}
	
	/**
	 * Updates passenger count based on load factor.
	 * @param pirepID the Flight Report database ID
	 * @param ud the Pilot's UserData object
	 * @return TRUE if the passenger count was updated, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean updatePaxCount(int pirepID, UserData ud) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("UPDATE ");
		buf.append(formatDBName(ud.getDB()));
		buf.append(".PIREPS P, common.AIRCRAFT_AIRLINE AA SET P.PAX=ROUND(AA.SEATS*P.LOADFACTOR, 0) WHERE (P.ID=?) AND (P.EQTYPE=AA.NAME) AND (AA.AIRLINE=?) AND (ABS(P.PAX-(AA.SEATS*P.LOADFACTOR))>1)");
		
		try (PreparedStatement ps = prepareWithoutLimits(buf.toString())) {
			ps.setInt(1, pirepID);
			ps.setString(2, ud.getAirlineCode());
			return (executeUpdate(ps, 0) > 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes SimBrief dispatch data to the database.
	 * @param sb the SimBrief package
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeSimBrief(BriefingPackage sb) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO PIREP_SIMBRIEF (ID, SIMBRIEF_ID, AIRAC, CREATED, FUEL, OFPTYPE, RUNWAY_D, RUNWAY_A, TAILCODE, AIRFRAME_ID, ROUTE, XML) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
			ps.setInt(1, sb.getID());
			ps.setString(2, sb.getSimBriefID());
			ps.setInt(3, sb.getAIRAC());
			ps.setTimestamp(4, createTimestamp(sb.getCreatedOn()));
			ps.setInt(5, sb.getTotalFuel());
			ps.setInt(6, sb.getFormat().ordinal());
			ps.setString(7, sb.getRunwayD());
			ps.setString(8, sb.getRunwayA());
			ps.setString(9, sb.getTailCode());
			ps.setString(10, sb.getAirframeID());
			ps.setString(11, sb.getRoute());
			ps.setString(12, XMLUtils.format(sb.getXML(), "UTF-8"));
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes Elite program scores to the database.
	 * @param sc the FlightEliteScore
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeElite(FlightEliteScore sc, String dbName) throws DAOException {
		String db = formatDBName(dbName);
		try {
			startTransaction();
		
			// Write the core entry
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO " + db + ".PIREP_ELITE (ID, LEVEL, YEAR, DISTANCE, SCORE, SCORE_ONLY) VALUES (?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, sc.getID());
				ps.setString(2, sc.getEliteLevel());
				ps.setInt(3, sc.getYear());
				ps.setInt(4, sc.getDistance());
				ps.setInt(5, sc.getPoints());
				ps.setBoolean(6, sc.getScoreOnly());
				executeUpdate(ps, 1);
			}
		
			// Write the child rows
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO " + db + ".PIREP_ELITE_ENTRIES (ID, SEQ, SCORE, BONUS, REMARKS) VALUES (?, ?, ?, ?, ?)")) {
				ps.setInt(1, sc.getID());
				for (EliteScoreEntry ese : sc.getEntries()) {
					ps.setInt(2, ese.getSequence());
					ps.setInt(3, ese.getPoints());
					ps.setBoolean(4, ese.isBonus());
					ps.setString(5, ese.getMessage());
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, sc.getEntries().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			YearlyTotal yt = new YearlyTotal(sc.getYear(), sc.getAuthorID());
			CacheManager.invalidate("EliteLifetimeTotal", Integer.valueOf(sc.getAuthorID()));
			CacheManager.invalidate("EliteYearlyTotal", yt.cacheKey());
			CacheManager.invalidate("EliteYearlyTotal", Integer.valueOf(sc.getAuthorID()));
		}
	}
	
	/*
	 * Helper method to write promotion equipment types.
	 */
	private void writePromoEQ(int id, String dbName, Collection<String> eqTypes) throws SQLException {
		
		// Delete the existing records
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM " + dbName + ".PROMO_EQ WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		}

		// Write the new records
		if (!eqTypes.isEmpty()) {
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO " + dbName + ".PROMO_EQ (ID, EQTYPE) VALUES (?, ?)")) {
				ps.setInt(1, id);
				for (String eqType : eqTypes) {
					ps.setString(2, eqType);
					ps.addBatch();
				}

				executeUpdate(ps, 1, eqTypes.size());
			}
		}
	}

	/*
	 * Helper method to write a Flight Report's core data to the database.
	 */
	private void writeCore(FlightReport fr, String dbName) throws SQLException {

		// Initialize the prepared statement
		if (fr.getID() == 0)
			insert(fr, dbName);
		else
			update(fr, dbName);

		// If we are writing a new PIREP get the database ID
		if (fr.getID() == 0)
			fr.setID(getNewID());

		// Write the route into the database
		if (!StringUtils.isEmpty(fr.getRoute())) {
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO " + dbName + ".PIREP_ROUTE (ID, ROUTE) VALUES (?, ?)")) {
				ps.setInt(1, fr.getID());
			
				// Strip out anything not a letter or digit
				String rt = fr.getRoute().toUpperCase();
				StringBuilder buf = new StringBuilder(); char lastChar = ' ';
				for (int x = 0; x < rt.length(); x++) {
					char c = rt.charAt(x);
					if (Character.isDigit(c) || Character.isLetter(c)) {
						buf.append(c);
						lastChar = c;
					} else if ((Character.isWhitespace(c) || (c == '.')) && !Character.isWhitespace(lastChar)) {
						buf.append(' ');
						lastChar = ' ';
					}
				}
			
				ps.setString(2, buf.toString());
				executeUpdate(ps, 1);
			}
		} else {
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM " + dbName + ".PIREP_ROUTE WHERE (ID=?)")) {
				ps.setInt(1, fr.getID());
				executeUpdate(ps, 0);
			}
		}
	}
	
	/*
	 * Writes flight report comments to the database.
	 */
	private void writeComments(FlightReport fr, String dbName) throws SQLException {
		boolean isEmpty = StringUtils.isEmpty(fr.getRemarks()) && StringUtils.isEmpty(fr.getComments());
		if (!isEmpty) {
			try (PreparedStatement ps = prepare("REPLACE INTO " + dbName + ".PIREP_COMMENT (ID, COMMENTS, REMARKS) VALUES (?, ?, ?)")) {
				ps.setInt(1, fr.getID());
				ps.setString(2, fr.getComments());
				ps.setString(3, fr.getRemarks());
				executeUpdate(ps, 1);
			}
		} else {
			try (PreparedStatement ps = prepare("DELETE FROM " + dbName + ".PIREP_COMMENT WHERE (ID=?)")) {
				ps.setInt(1, fr.getID());
				executeUpdate(ps, 0);
			}
		}
	}

	/**
	 * Write a Flight Report to the default database.
	 * @param fr the Flight Report
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(FlightReport fr) throws DAOException {
		write(fr, SystemData.get("airline.db"));
	}
	
	/**
	 * Writes a Flight Report to the database.
	 * @param fr the Flight Report
	 * @param db the Database to write to
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(FlightReport fr, String db) throws DAOException {
		String dbName = formatDBName(db);
		try {
			startTransaction();
			writeCore(fr, dbName);
			writeComments(fr, dbName);
			writePromoEQ(fr.getID(), dbName, fr.getCaptEQType());
			writeDraft(fr, dbName);
			writeHistory(fr.getStatusUpdates(), dbName);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Writes an ACARS-enabled Flight Report into the database. This can handle INSERT and UPDATE operations.
	 * @param fr the Flight Report
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeACARS(FDRFlightReport fr, String dbName) throws DAOException {
		String db = formatDBName(dbName);
		boolean isACARS = (fr instanceof ACARSFlightReport);

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("REPLACE INTO ");
		sqlBuf.append(db);
		sqlBuf.append(".ACARS_PIREPS (ID, ACARS_ID, START_TIME, START_LAT, START_LNG, TAXI_TIME, TAXI_WEIGHT, TAXI_FUEL, TAKEOFF_TIME, TAKEOFF_DISTANCE, TAKEOFF_SPEED, TAKEOFF_N1, TAKEOFF_HDG, TAKEOFF_LAT, TAKEOFF_LNG, TAKEOFF_ALT, "
			+ "TAKEOFF_WEIGHT, TAKEOFF_FUEL, LANDING_TIME, LANDING_DISTANCE, LANDING_SPEED, LANDING_VSPEED, LANDING_N1, LANDING_HDG, LANDING_LAT, LANDING_LNG, LANDING_ALT, LANDING_WEIGHT, LANDING_FUEL, LANDING_SCORE, END_TIME, GATE_LAT, "
			+ "GATE_LNG, GATE_WEIGHT, GATE_FUEL, TOTAL_FUEL, TIME_0X, TIME_1X, TIME_2X, TIME_4X, SDK, RESTORE_COUNT, CLIENT_BUILD, BETA_BUILD, LANDING_G, LANDING_CAT, FRAMERATE, PAX_WEIGHT, CARGO_WEIGHT, CAPABILITIES, TIME_BOARD, "
			+ "TIME_DEBOARD, TIME_ONLINE, TAILCODE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		try {
			startTransaction();

			// Write the regular fields
			writeCore(fr, db);
			writeComments(fr, db);
			writePromoEQ(fr.getID(), db, fr.getCaptEQType());

			// Write the ACARS fields
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				ps.setInt(1, fr.getID());
				ps.setInt(2, fr.getDatabaseID(DatabaseID.ACARS));
				ps.setTimestamp(3, createTimestamp(fr.getStartTime()));
				ps.setDouble(4, fr.getStartLocation().getLatitude());
				ps.setDouble(5, fr.getStartLocation().getLongitude());
				ps.setTimestamp(6, createTimestamp(fr.getTaxiTime()));
				ps.setInt(7, fr.getTaxiWeight());
				ps.setInt(8, fr.getTaxiFuel());
				ps.setTimestamp(9, createTimestamp(fr.getTakeoffTime()));
				ps.setInt(10, fr.getTakeoffDistance());
				ps.setInt(11, fr.getTakeoffSpeed());
				ps.setDouble(12, fr.getTakeoffN1());
				ps.setInt(13, fr.getTakeoffHeading());
				ps.setDouble(14, fr.getTakeoffLocation().getLatitude());
				ps.setDouble(15, fr.getTakeoffLocation().getLongitude());
				ps.setInt(16, fr.getTakeoffLocation().getAltitude());
				ps.setInt(17, fr.getTakeoffWeight());
				ps.setInt(18, fr.getTakeoffFuel());
				ps.setTimestamp(19, createTimestamp(fr.getLandingTime()));
				ps.setInt(20, fr.getLandingDistance());
				ps.setInt(21, fr.getLandingSpeed());
				ps.setInt(22, fr.getLandingVSpeed());
				ps.setDouble(23, fr.getLandingN1());
				ps.setInt(24, fr.getLandingHeading());
				ps.setDouble(25, fr.getLandingLocation().getLatitude());
				ps.setDouble(26, fr.getLandingLocation().getLongitude());
				ps.setInt(27, fr.getLandingLocation().getAltitude());
				ps.setInt(28, fr.getLandingWeight());
				ps.setInt(29, fr.getLandingFuel());
				ps.setTimestamp(31, createTimestamp(fr.getEndTime()));
				ps.setDouble(32, fr.getEndLocation().getLatitude());
				ps.setDouble(33, fr.getEndLocation().getLongitude());
				ps.setInt(34, fr.getGateWeight());
				ps.setInt(35, fr.getGateFuel());
				ps.setInt(36, fr.getTotalFuel());
				
				// ACARS
				if (isACARS) {
					ACARSFlightReport afr = (ACARSFlightReport) fr;
					ps.setInt(30, (int)Math.round(afr.getLandingScore() * 100));
					ps.setInt(37, afr.getTime(0));
					ps.setInt(38, afr.getTime(1));
					ps.setInt(39, afr.getTime(2));
					ps.setInt(40, afr.getTime(4));
					ps.setString(41, afr.getSDK());
					ps.setInt(42, afr.getRestoreCount());
					ps.setInt(43, afr.getClientBuild());
					ps.setInt(44, afr.getBeta());
					ps.setDouble(45, afr.getLandingG());
					ps.setInt(46, afr.getLandingCategory().ordinal());
					ps.setInt(47, (int)(afr.getAverageFrameRate() * 10));
					ps.setInt(48, afr.getPaxWeight());
					ps.setInt(49, afr.getCargoWeight());
					ps.setLong(50, afr.getCapabilities());
					ps.setLong(51, afr.getBoardTime().toSeconds());
					ps.setLong(52, afr.getDeboardTime().toSeconds());
					ps.setLong(53, afr.getOnlineTime().toSeconds());
					ps.setString(54,  afr.getTailCode());
				} else if (fr instanceof XACARSFlightReport xfr) {
					ps.setInt(30, -1);
					ps.setInt(37, 0);
					ps.setInt(38, 0);
					ps.setInt(39, 0);
					ps.setInt(40, 0);
					ps.setString(41, null);
					ps.setBoolean(42, false);
					ps.setInt(43, xfr.getMajorVersion());
					ps.setInt(44, xfr.getMinorVersion());
					ps.setDouble(45, 0);
					ps.setInt(46, 0);
					ps.setInt(47, 0);
					ps.setInt(48, 0);
					ps.setInt(49, 0);
					ps.setLong(50, 0);
					ps.setInt(51, 0);
					ps.setInt(52, 0);
					ps.setInt(53, 0);
					ps.setString(54, null);
				}

				executeUpdate(ps, 1);
			}
			
			// Write Metadata
			if (isACARS) {
				ACARSFlightReport afr = (ACARSFlightReport) fr;
				sqlBuf = new StringBuilder("REPLACE INTO ");
				sqlBuf.append(db);
				sqlBuf.append(".ACARS_METADATA (ID, AUTHOR, ACPATH, FDE, CODE) VALUES (?, ?, ?, ?, ?)");
				try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
					ps.setInt(1, afr.getID());
					ps.setString(2, afr.getAuthor());
					ps.setString(3, afr.getAircraftPath());
					ps.setString(4, StringUtils.isEmpty(afr.getFDE()) ? null : afr.getFDE());
					ps.setString(5, StringUtils.isEmpty(afr.getAircraftCode()) ? null : afr.getAircraftCode());
					executeUpdate(ps, 1);
				}
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Flight Report's status updates to the database.
	 * @param upds a Collection of FlightHistoryEntry beans
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void writeHistory(Collection<FlightHistoryEntry> upds, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("INSERT INTO ");
		buf.append(formatDBName(dbName));
		buf.append(".PIREP_STATUS_HISTORY (ID, AUTHOR_ID, UPDATE_TYPE, CREATEDON, DESCRIPTION) VALUES (?, ?, ?, ?, ?)");
		
		try (PreparedStatement ps = prepareWithoutLimits(buf.toString())) {
			Instant lastUpdateTime = Instant.MIN;
			for (FlightHistoryEntry upd : upds) {
				Instant updDate = upd.getDate().truncatedTo(ChronoUnit.MILLIS);
				if (!updDate.isAfter(lastUpdateTime))
					updDate = lastUpdateTime.plusMillis(5);
				
				// Write the data
				ps.setInt(1, upd.getID());
				ps.setInt(2, upd.getAuthorID());
				ps.setInt(3, upd.getType().ordinal());
				ps.setTimestamp(4, createTimestamp(updDate));
				ps.setString(5, upd.getDescription());
				ps.addBatch();
				lastUpdateTime = updDate;
			}
			
			executeUpdate(ps, 1, upds.size());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}