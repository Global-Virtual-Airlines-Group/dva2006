// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.stats.Tour;
import org.deltava.beans.schedule.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read Tour data from the database.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class GetTour extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetTour(Connection c) {
		super(c);
	}

	/**
	 * Loads a Tour from the database.
	 * @param id the database ID
	 * @param dbName the database name
	 * @return a Tour or null if not found
	 * @throws DAOException
	 */
	public Tour get(int id, String dbName) throws DAOException {
		
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT T.*, GROUP_CONCAT(TN.NETWORK), TB.SIZE, TB.ISPDF FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".TOURS T LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".TOUR_NETWORKS TN ON (T.ID=TN.ID) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".TOUR_BRIEFINGS TB ON (T.ID=TB.ID) WHERE (T.ID=?) GROUP BY T.ID LIMIT 1");
		
		try {
			Tour t = null;
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				ps.setInt(1, id);
				t = execute(ps).stream().findFirst().orElse(null);
			}
			
			if (t == null) return null;
			t.setOwner(SystemData.getApp(dbName));
			loadLegs(t);
			loadProgress(t);
			loadBriefing(t);
			return t;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves all Tours from the database.
	 * @return a Collection of Tour beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Tour> getAll() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT T.*, GROUP_CONCAT(TN.NETWORK), TB.SIZE, TB.ISPDF, (SELECT COUNT(TL.IDX) FROM TOUR_LEGS TL WHERE (TL.ID=T.ID)) AS LEGCNT FROM TOURS T "
			+ "LEFT JOIN TOUR_NETWORKS TN ON (T.ID=TN.ID) LEFT JOIN TOUR_BRIEFINGS TB ON (T.ID=TB.ID) GROUP BY T.ID")) {
			List<Tour> results = execute(ps);
			results.forEach(t -> t.setOwner(SystemData.getApp(null)));
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves all Tours that contain a particular Flight leg. 
	 * @param rp the RoutePair
	 * @param dt the flight date
	 * @param dbName the database name
	 * @return a Collection of Tour beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Tour> findLeg(RoutePair rp, Instant dt, String dbName) throws DAOException {
		
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT T.*, GROUP_CONCAT(TN.NETWORK), TB.SIZE, TB.ISPDF FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".TOUR_LEGS TL, ");
		sqlBuf.append(db);
		sqlBuf.append(".TOURS T LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".TOUR_NETWORKS TN ON (T.ID=TN.ID) LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".TOUR_BRIEFINGS TB ON (T.ID=TB.ID) WHERE (T.ID=TL.ID) AND (T.ACTIVE=?) AND (TL.AIRPORT_D=?) AND (TL.AIRPORT_A=?) HAVING (T.ID IS NOT NULL)");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setBoolean(1, true);
			ps.setString(2, rp.getAirportD().getIATA());
			ps.setString(3, rp.getAirportA().getIATA());
			Collection<Tour> results = execute(ps).stream().filter(t -> t.isActiveOn(dt)).collect(Collectors.toList());
			for (Tour t : results) {
				t.setOwner(SystemData.getApp(dbName));
				loadLegs(t);
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to load Tour flight legs.
	 */
	private void loadLegs(Tour t) throws SQLException {
		
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
		sqlBuf.append(t.getOwner().getDB());
		sqlBuf.append(".TOUR_LEGS WHERE (ID=?) ORDER BY IDX");
		
		long effectiveDate = LocalDate.now().toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC);
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, t.getID());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ScheduleEntry se = new ScheduleEntry(SystemData.getAirline(rs.getString(3)), rs.getInt(4), rs.getInt(5));
					se.setAirportD(SystemData.getAirport(rs.getString(6)));
					se.setAirportA(SystemData.getAirport(rs.getString(7)));
					se.setEquipmentType(rs.getString(8));
					se.setTimeD(rs.getTimestamp(9).toLocalDateTime().plusSeconds(effectiveDate));
					se.setTimeA(rs.getTimestamp(10).toLocalDateTime().plusSeconds(effectiveDate));
					t.addFlight(se);
				}
			}
		}
	}
	
	/*
	 * Helper method to parse Tour result sets.
	 */
	private static List<Tour> execute(PreparedStatement ps) throws SQLException {
		List<Tour> results = new ArrayList<Tour>();
		try (ResultSet rs = ps.executeQuery()) {
			ResultSetMetaData md = rs.getMetaData();
			boolean hasNetworks = (md.getColumnCount() > 9);
			boolean hasBriefingInfo = (md.getColumnCount() > 11);
			boolean hasLegCount = (md.getColumnCount() > 12);
			while (rs.next()) {
				Tour t = new Tour(rs.getString(2));
				t.setID(rs.getInt(1));
				t.setStartDate(toInstant(rs.getTimestamp(3)));
				t.setEndDate(toInstant(rs.getTimestamp(4)));
				t.setActive(rs.getBoolean(5));
				t.setACARSOnly(rs.getBoolean(6));
				t.setAllowOffline(rs.getBoolean(7));
				t.setMatchEquipment(rs.getBoolean(8));
				t.setMatchLeg(rs.getBoolean(9));
				if (hasLegCount) t.setFlightCount(rs.getInt(13));
				if (hasBriefingInfo) {
					t.setForceSize(rs.getInt(11));
					t.setForcePDF(rs.getBoolean(12));
				}
				
				if (hasNetworks) {
					Collection<String> networks = StringUtils.split(rs.getString(10), ",");
					if (networks != null)
						networks.stream().map(id -> OnlineNetwork.values()[Integer.parseInt(id)]).forEach(t::addNetwork);
				}
				
				results.add(t);
			}
		}
		
		return results;
	}

	/*
	 * Helper method to load Pilot progress in a Tour.
	 */
	private void loadProgress(Tour t) throws SQLException {
		
		StringBuilder sqlBuf = new StringBuilder("SELECT P.PILOT_ID, COUNT(P.ID) FROM ");
		sqlBuf.append(t.getOwner().getDB());
		sqlBuf.append(".PIREPS P WHERE (P.TOUR_ID=?) AND ((P.STATUS=?) OR (P.STATUS=?) OR (P.STATUS=?)) GROUP BY P.PILOT_ID");
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, t.getID());
			ps.setInt(2, FlightStatus.OK.ordinal());
			ps.setInt(3, FlightStatus.SUBMITTED.ordinal());
			ps.setInt(4, FlightStatus.HOLD.ordinal());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					t.addPilot(rs.getInt(1), rs.getInt(2));
			}
		}
	}
	
	/*
	 * Helper method to load a Tour briefing document.
	 */
	private void loadBriefing(Tour t) throws SQLException {
		
		StringBuilder sqlBuf = new StringBuilder("SELECT DATA FROM ");
		sqlBuf.append(t.getOwner().getDB());
		sqlBuf.append(".TOUR_BRIEFINGS WHERE (ID=?)");
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setInt(1, t.getID());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					t.load(rs.getBytes(1));
			}
		}
	}
}