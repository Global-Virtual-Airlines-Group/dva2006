// Copyright 2015, 2016, 2017, 2018, 2019, 2020, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to update Flight Statistics. 
 * @author Luke
 * @version 10.4
 * @since 6.2
 */

public class SetAggregateStatistics extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAggregateStatistics(Connection c) {
		super(c);
	}

	/**
	 * Updates flight statistics based linked to a flight report.
	 * @param fr the FlightReport
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(FlightReport fr) throws DAOException {
		try {
			startTransaction();
			updatePilot(fr.getAuthorID());
			updateAirport(fr.getAirportD(), true);
			updateAirport(fr.getAirportA(), false);
			updateRoute(fr.getAuthorID(), fr);
			updateEQ(fr.getEquipmentType());
			updateDate(fr.getDate());
			updatePilotDay(fr);
			updateNetwork(fr);
			if (fr.hasAttribute(FlightReport.ATTR_ACARS))
				updateLanding(fr);
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/*
	 * Updates ACARS landing statistics for a particular flight.
	 */
	public void updateLanding(FlightReport fr) throws DAOException {
		try {
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM FLIGHTSTATS_LANDING WHERE (ID=?)")) {
				ps.setInt(1, fr.getID());
				executeUpdate(ps, 0);
				if (fr.getStatus() != FlightStatus.OK)
					return;
			}
			
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO FLIGHTSTATS_LANDING (SELECT PR.ID, PR.PILOT_ID, PR.EQTYPE, DATE(APR.LANDING_TIME), PR.AIRPORT_A, APR.LANDING_VSPEED, "
				+ "R.DISTANCE, APR.LANDING_SCORE FROM PIREPS PR, ACARS_PIREPS APR, acars.RWYDATA R WHERE (APR.ID=PR.ID) AND (APR.ACARS_ID=R.ID) AND (R.ISTAKEOFF=?) AND (R.DISTANCE<?) AND (PR.ID=?))")) {
				ps.setBoolean(1, false);
				ps.setInt(2, 32500);
				ps.setInt(3, fr.getID());
				executeUpdate(ps, 0);
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Updates flight statistics for a particular Pilot.
	 */
	private void updatePilot(int pilotID) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO FLIGHTSTATS_PILOT (SELECT PILOT_ID, COUNT(DISTANCE) AS LEGS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS ACARS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS VATSIM, "
			+ "SUM(IF((ATTR & ?) > 0, 1, 0)) AS IVAO, SUM(IF((ATTR & ?) > 0, 1, 0)) AS HIST, SUM(IF((ATTR & ?) > 0, 1, 0)) AS DSP, SUM(IF((ATTR & ?) > 0, 1, 0)) AS SB, SUM(IF(TOUR_ID > 0, 1, 0)) AS TOUR, SUM(DISTANCE) AS MILES, "
			+ "SUM(FLIGHT_TIME) AS HOURS, 1 AS PIDS, AVG(LOADFACTOR), SUM(PAX), SUM(IF(FSVERSION=?,1,0)) AS FS7, SUM(IF(FSVERSION=?,1,0)) AS FS8, SUM(IF(FSVERSION=?,1,0)) AS FS9, SUM(IF(FSVERSION=?,1,0)) AS FSX, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS P3D, SUM(IF(FSVERSION=?,1,0)) AS P3Dv4, SUM(IF(FSVERSION=?,1,IF(FSVERSION=?,1,0))) AS XP, SUM(IF(FSVERSION=?,1,0)) AS XP11, SUM(IF(FSVERSION=?,1,0)) AS XP12, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS FS20, SUM(IF(FSVERSION=0,1,0)) AS FSO FROM PIREPS WHERE (STATUS=?) AND (PILOT_ID=?) HAVING (PILOT_ID IS NOT NULL))")) {
			ps.setInt(1, FlightReport.ATTR_ACARS);
			ps.setInt(2, FlightReport.ATTR_VATSIM);
			ps.setInt(3, FlightReport.ATTR_IVAO);
			ps.setInt(4, FlightReport.ATTR_HISTORIC);
			ps.setInt(5, FlightReport.ATTR_DISPATCH);
			ps.setInt(6, FlightReport.ATTR_SIMBRIEF);
			ps.setInt(7, Simulator.FS2000.getCode());
			ps.setInt(8, Simulator.FS2002.getCode());
			ps.setInt(9, Simulator.FS9.getCode());
			ps.setInt(10, Simulator.FSX.getCode());
			ps.setInt(11, Simulator.P3D.getCode());
			ps.setInt(12, Simulator.P3Dv4.getCode());
			ps.setInt(13, Simulator.XP9.getCode());
			ps.setInt(14, Simulator.XP10.getCode());
			ps.setInt(15, Simulator.XP11.getCode());
			ps.setInt(16, Simulator.XP12.getCode());
			ps.setInt(17, Simulator.FS2020.getCode());
			ps.setInt(18, FlightStatus.OK.ordinal());
			ps.setInt(19, pilotID);
			executeUpdate(ps, 0);
		}
	}
	
	private void updatePilotDay(FlightReport fr) throws SQLException {
		
		// Clean up in case we delete
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM FLIGHTSTATS_PILOT_DAY WHERE (DATE=?) AND (PILOT_ID=?)")) {
			ps.setTimestamp(1, createTimestamp(fr.getDate()));
			ps.setInt(2, fr.getAuthorID());
			executeUpdate(ps, 0);
		}
		
		if (fr.getStatus() == FlightStatus.OK) {
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO FLIGHTSTATS_PILOT_DAY (DATE, PILOT_ID) VALUES (?, ?)")) {
				ps.setTimestamp(1, createTimestamp(fr.getDate()));
				ps.setInt(2, fr.getAuthorID());
				executeUpdate(ps, 1);
			}
		}
	}

	/*
	 * Updates flight statistics for a particular route pair.
	 */
	private void updateRoute(int pilotID, RoutePair rp) throws SQLException {
		
		// Clean up in case we are deleting
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM FLIGHTSTATS_ROUTES WHERE (PILOT_ID=?) AND (AIRPORT_D=?) AND (AIRPORT_A=?)")) {
			ps.setInt(1, pilotID);
			ps.setString(2, rp.getAirportD().getIATA());
			ps.setString(3, rp.getAirportA().getIATA());
			executeUpdate(ps, 0);
		}
		
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO FLIGHTSTATS_ROUTES (SELECT PILOT_ID, AIRPORT_D, AIRPORT_A, COUNT(ID), MAX(DATE) FROM PIREPS WHERE (STATUS=?) AND "
			+ "(PILOT_ID=?) AND (AIRPORT_D=?) AND (AIRPORT_A=?) HAVING (PILOT_ID IS NOT NULL))")) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setInt(2, pilotID);
			ps.setString(3, rp.getAirportD().getIATA());
			ps.setString(4, rp.getAirportA().getIATA());
			executeUpdate(ps, 0);
		}
	}
	
	/*
	 * Updates flight statistics for a particular Airport.
	 */
	private void updateAirport(Airport a, boolean isDeparture) throws SQLException {
		String apColumn = isDeparture ? "AIRPORT_D" : "AIRPORT_A";
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO FLIGHTSTATS_AIRPORT (SELECT ?, COUNT(DISTANCE) AS LEGS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS ACARS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS VATSIM, "
			+ "SUM(IF((ATTR & ?) > 0, 1, 0)) AS IVAO, SUM(IF((ATTR & ?) > 0, 1, 0)) AS HIST, SUM(IF((ATTR & ?) > 0, 1, 0)) AS DSP, SUM(IF((ATTR & ?) > 0, 1, 0)) AS SB, SUM(IF(TOUR_ID > 0, 1, 0)) AS TOUR, SUM(DISTANCE) AS MILES, "
			+ "SUM(FLIGHT_TIME) AS HOURS, COUNT(DISTINCT PILOT_ID) AS PIDS, AVG(LOADFACTOR), SUM(PAX), SUM(IF(FSVERSION=?,1,0)) AS FS7, SUM(IF(FSVERSION=?,1,0)) AS FS8, SUM(IF(FSVERSION=?,1,0)) AS FS9, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS FSX, SUM(IF(FSVERSION=?,1,0)) AS P3D, SUM(IF(FSVERSION=?,1,0)) AS P3Dv4, SUM(IF(FSVERSION=?,1,IF(FSVERSION=?,1,0))) AS XP, SUM(IF(FSVERSION=?,1,0)) AS XP11, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS XP12, SUM(IF(FSVERSION=?,1,0)) AS FS20, SUM(IF(FSVERSION=0,1,0)) AS FSO, ? FROM PIREPS WHERE (STATUS=?) AND (" + apColumn + "=?) HAVING (ACARS IS NOT NULL))")) {
			ps.setString(1, a.getIATA());
			ps.setInt(2, FlightReport.ATTR_ACARS);
			ps.setInt(3, FlightReport.ATTR_VATSIM);
			ps.setInt(4, FlightReport.ATTR_IVAO);
			ps.setInt(5, FlightReport.ATTR_HISTORIC);
			ps.setInt(6, FlightReport.ATTR_DISPATCH);
			ps.setInt(7, FlightReport.ATTR_SIMBRIEF);
			ps.setInt(8, Simulator.FS2000.getCode());
			ps.setInt(9, Simulator.FS2002.getCode());
			ps.setInt(10, Simulator.FS9.getCode());
			ps.setInt(11, Simulator.FSX.getCode());
			ps.setInt(12, Simulator.P3D.getCode());
			ps.setInt(13, Simulator.P3Dv4.getCode());
			ps.setInt(14, Simulator.XP9.getCode());
			ps.setInt(15, Simulator.XP10.getCode());
			ps.setInt(16, Simulator.XP11.getCode());
			ps.setInt(17, Simulator.XP12.getCode());
			ps.setInt(18, Simulator.FS2020.getCode());
			ps.setBoolean(19, isDeparture);
			ps.setInt(20, FlightStatus.OK.ordinal());
			ps.setString(21, a.getIATA());
			executeUpdate(ps, 0);
		}
	}
	
	/*
	 * Updates flight statistics for a particular Date.
	 */
	private void updateDate(java.time.Instant dt) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO FLIGHTSTATS_DATE (SELECT DATE, COUNT(DISTANCE) AS LEGS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS ACARS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS VATSIM, "
			+ "SUM(IF((ATTR & ?) > 0, 1, 0)) AS IVAO, SUM(IF((ATTR & ?) > 0, 1, 0)) AS HIST, SUM(IF((ATTR & ?) > 0, 1, 0)) AS DSP, SUM(IF((ATTR & ?) > 0, 1, 0)) AS SB, SUM(IF(TOUR_ID > 0, 1, 0)) AS TOUR, SUM(DISTANCE) AS MILES, "
			+ "SUM(FLIGHT_TIME) AS HOURS, COUNT(DISTINCT PILOT_ID) AS PIDS, AVG(LOADFACTOR), SUM(PAX), SUM(IF(FSVERSION=?,1,0)) AS FS7, SUM(IF(FSVERSION=?,1,0)) AS FS8, SUM(IF(FSVERSION=?,1,0)) AS FS9, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS FSX, SUM(IF(FSVERSION=?,1,0)) AS P3D, SUM(IF(FSVERSION=?,1,0)) AS P3Dv4, SUM(IF(FSVERSION=?,1,IF(FSVERSION=?,1,0))) AS XP, SUM(IF(FSVERSION=?,1,0)) AS XP11, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS XP12, SUM(IF(FSVERSION=?,1,0)) AS FS20, SUM(IF(FSVERSION=0,1,0)) AS FSO FROM PIREPS WHERE (STATUS=?) AND (DATE=DATE(?)))")) {
			ps.setInt(1, FlightReport.ATTR_ACARS);
			ps.setInt(2, FlightReport.ATTR_VATSIM);
			ps.setInt(3, FlightReport.ATTR_IVAO);
			ps.setInt(4, FlightReport.ATTR_HISTORIC);
			ps.setInt(5, FlightReport.ATTR_DISPATCH);
			ps.setInt(6, FlightReport.ATTR_SIMBRIEF);
			ps.setInt(7, Simulator.FS2000.getCode());
			ps.setInt(8, Simulator.FS2002.getCode());
			ps.setInt(9, Simulator.FS9.getCode());
			ps.setInt(10, Simulator.FSX.getCode());
			ps.setInt(11, Simulator.P3D.getCode());
			ps.setInt(12, Simulator.P3Dv4.getCode());
			ps.setInt(13, Simulator.XP9.getCode());
			ps.setInt(14, Simulator.XP10.getCode());
			ps.setInt(15, Simulator.XP11.getCode());
			ps.setInt(16, Simulator.XP12.getCode());
			ps.setInt(17, Simulator.FS2020.getCode());
			ps.setInt(18, FlightStatus.OK.ordinal());
			ps.setTimestamp(19, createTimestamp(dt));
			executeUpdate(ps, 0);
		}
	}
	
	/*
	 * Updates flight statistics for a particular equipment type.
	 */
	private void updateEQ(String eqType) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO FLIGHTSTATS_EQTYPE (SELECT EQTYPE, COUNT(DISTANCE) AS LEGS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS ACARS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS VATSIM, "
			+ "SUM(IF((ATTR & ?) > 0, 1, 0)) AS IVAO, SUM(IF((ATTR & ?) > 0, 1, 0)) AS HIST, SUM(IF((ATTR & ?) > 0, 1, 0)) AS DSP, SUM(IF((ATTR & ?) > 0, 1, 0)) AS SB, SUM(IF(TOUR_ID > 0, 1, 0)) AS TOUR, SUM(DISTANCE) AS MILES, "
			+ "SUM(FLIGHT_TIME) AS HOURS, COUNT(DISTINCT PILOT_ID) AS PIDS, AVG(LOADFACTOR), SUM(PAX), SUM(IF(FSVERSION=?,1,0)) AS FS7, SUM(IF(FSVERSION=?,1,0)) AS FS8, SUM(IF(FSVERSION=?,1,0)) AS FS9, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS FSX, SUM(IF(FSVERSION=?,1,0)) AS P3D, SUM(IF(FSVERSION=?,1,0)) AS P3Dv4, SUM(IF(FSVERSION=?,1,IF(FSVERSION=?,1,0))) AS XP, SUM(IF(FSVERSION=?,1,0)) AS XP11, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS XP12, SUM(IF(FSVERSION=?,1,0)) AS FS20, SUM(IF(FSVERSION=0,1,0)) AS FSO FROM PIREPS WHERE (STATUS=?) AND (EQTYPE=?) HAVING (EQTYPE IS NOT NULL))")) {
			ps.setInt(1, FlightReport.ATTR_ACARS);
			ps.setInt(2, FlightReport.ATTR_VATSIM);
			ps.setInt(3, FlightReport.ATTR_IVAO);
			ps.setInt(4, FlightReport.ATTR_HISTORIC);
			ps.setInt(5, FlightReport.ATTR_DISPATCH);
			ps.setInt(6, FlightReport.ATTR_SIMBRIEF);
			ps.setInt(7, Simulator.FS2000.getCode());
			ps.setInt(8, Simulator.FS2002.getCode());
			ps.setInt(9, Simulator.FS9.getCode());
			ps.setInt(10, Simulator.FSX.getCode());
			ps.setInt(11, Simulator.P3D.getCode());
			ps.setInt(12, Simulator.P3Dv4.getCode());
			ps.setInt(13, Simulator.XP9.getCode());
			ps.setInt(14, Simulator.XP10.getCode());
			ps.setInt(15, Simulator.XP11.getCode());
			ps.setInt(16, Simulator.XP12.getCode());
			ps.setInt(17, Simulator.FS2020.getCode());
			ps.setInt(18, FlightStatus.OK.ordinal());
			ps.setString(19, eqType);
			executeUpdate(ps, 0);
		}
	}

	/*
	 * Updates flight statistics for a particular online network.
	 */
	private void updateNetwork(FlightReport fr) throws SQLException {
		try (PreparedStatement ps = prepare("REPLACE INTO FLIGHTSTATS_NETWORK (SELECT DATE, IF((ATTR & ?) > 0, 0, IF((ATTR & ?) > 0, 1, IF((ATTR & ?) > 0, 5, IF((ATTR & ?) > 0, 6, -1)))) AS NET, "
			+ "COUNT(DISTANCE) AS LEGS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS ACARS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS HIST, SUM(IF((ATTR & ?) > 0, 1, 0)) AS DSP, SUM(IF((ATTR & ?) > 0, 1, 0)) AS SB, "
			+ "SUM(IF(TOUR_ID > 0, 1, 0)) AS TOUR, SUM(DISTANCE) AS MILES, SUM(FLIGHT_TIME) AS HOURS, COUNT(DISTINCT PILOT_ID) AS PIDS, AVG(LOADFACTOR), SUM(PAX) FROM PIREPS WHERE (STATUS=?) "
			+ "AND (DATE=DATE(?)) GROUP BY DATE, NET HAVING (NET=?))")) {
			ps.setInt(1, FlightReport.ATTR_VATSIM);
			ps.setInt(2, FlightReport.ATTR_IVAO);
			ps.setInt(3, FlightReport.ATTR_PEDGE);
			ps.setInt(4, FlightReport.ATTR_POSCON);
			ps.setInt(5, FlightReport.ATTR_ACARS);
			ps.setInt(6, FlightReport.ATTR_HISTORIC);
			ps.setInt(7, FlightReport.ATTR_DISPATCH);
			ps.setInt(8, FlightReport.ATTR_SIMBRIEF);
			ps.setInt(9, FlightStatus.OK.ordinal());
			ps.setTimestamp(10, createTimestamp(fr.getDate()));
			ps.setInt(11, (fr.getNetwork() == null) ? -1 : fr.getNetwork().ordinal());
			executeUpdate(ps, 0);
		}
	}
}