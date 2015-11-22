// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to update Flight Statistics. 
 * @author Luke
 * @version 6.3
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
			updatePilot(fr.getDatabaseID(DatabaseID.PILOT));
			updateAirport(fr.getAirportD(), true);
			updateAirport(fr.getAirportA(), false);
			updateRoute(fr.getDatabaseID(DatabaseID.PILOT), fr);
			updateEQ(fr.getEquipmentType());
			updateDate(fr.getDate());
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
	private void updateLanding(FlightReport fr) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM FLIGHTSTATS_LANDING WHERE (ID=?)");
			_ps.setInt(1, fr.getID());
			executeUpdate(0);
			if (fr.getStatus() != FlightReport.OK)
				return;
			
			prepareStatementWithoutLimits("INSERT INTO FLIGHTSTATS_LANDING (SELECT PR.ID, PR.PILOT_ID, PR.EQTYPE, PR.AIRPORT_A, "
				+ "APR.LANDING_VSPEED, CAST(R.DISTANCE AS SIGNED) FROM PIREPS PR, ACARS_PIREPS_APR, acars.RWYDATA R WHERE "
				+ "(APR.ID=PR.ID) AND (APR.ACARS_ID=R.ID) AND (R.ISTAKEOFF=?) AND (R.DISTANCE<?) AND (PR.ID=?))");
			_ps.setBoolean(1, false);
			_ps.setInt(2, 22500);
			_ps.setInt(3, fr.getID());
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Updates flight statistics for a particular Pilot.
	 */
	private void updatePilot(int pilotID) throws SQLException {
		prepareStatementWithoutLimits("REPLACE INTO FLIGHTSTATS_PILOT (SELECT PILOT_ID, COUNT(DISTANCE) AS LEGS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS ACARS, "
			+ "SUM(IF((ATTR & ?) > 0, 1, 0)) AS VATSIM, SUM(IF((ATTR & ?) > 0, 1, 0)) AS IVAO, SUM(IF((ATTR & ?) > 0, 1, 0)) AS HIST, SUM(IF((ATTR & ?) > 0, 1, 0)) AS DSP, "
			+ "SUM(DISTANCE) AS MILES, SUM(FLIGHT_TIME) AS HOURS, 1 AS PIDS, AVG(LOADFACTOR), SUM(PAX), SUM(IF(FSVERSION=?,1,0)) AS FS7, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS FS8, SUM(IF(FSVERSION=?,1,0)) AS FS9, SUM(IF(FSVERSION=?,1,0)) AS FSX, SUM(IF(FSVERSION=?,1,0)) AS P3D, "
			+ "SUM(IF(FSVERSION=?,1,IF(FSVERSION=?,1,0))) AS XP, SUM(IF(FSVERSION=0,1,0)) AS FSO FROM PIREPS WHERE (STATUS=?) AND (PILOT_ID=?) "
			+ "HAVING (PILOT_ID<>NULL))");
		_ps.setInt(1, FlightReport.ATTR_ACARS);
		_ps.setInt(2, FlightReport.ATTR_VATSIM);
		_ps.setInt(3, FlightReport.ATTR_IVAO);
		_ps.setInt(4, FlightReport.ATTR_HISTORIC);
		_ps.setInt(5, FlightReport.ATTR_DISPATCH);
		_ps.setInt(6, Simulator.FS2000.getCode());
		_ps.setInt(7, Simulator.FS2002.getCode());
		_ps.setInt(8, Simulator.FS9.getCode());
		_ps.setInt(9, Simulator.FSX.getCode());
		_ps.setInt(10, Simulator.P3D.getCode());
		_ps.setInt(11, Simulator.XP9.getCode());
		_ps.setInt(12, Simulator.XP10.getCode());
		_ps.setInt(13, FlightReport.OK);
		_ps.setInt(14, pilotID);
		executeUpdate(0);
	}

	/*
	 * Updates flight statistics for a particular route pair.
	 */
	private void updateRoute(int pilotID, RoutePair rp) throws SQLException {
		
		// Clean up in case we are deleting
		prepareStatementWithoutLimits("DELETE FROM FLIGHTSTATS_ROUTES WHERE (PILOT_ID=?) AND (AIRPORT_D=?) AND (AIRPORT_A=?)");
		_ps.setInt(1, pilotID);
		_ps.setString(2, rp.getAirportD().getIATA());
		_ps.setString(3, rp.getAirportA().getIATA());
		executeUpdate(0);
		
		prepareStatementWithoutLimits("INSERT INTO FLIGHTSTATS_ROUTES (SELECT PILOT_ID, AIRPORT_D, AIRPORT_A, COUNT(ID), MAX(DATE) FROM "
			+ "PIREPS WHERE (STATUS=?) AND (PILOT_ID=?) AND (AIRPORT_D=?) AND (AIRPORT_A=?) HAVING (PILOT_ID<>NULL))");
		_ps.setInt(1, FlightReport.OK);
		_ps.setInt(2, pilotID);
		_ps.setString(3, rp.getAirportD().getIATA());
		_ps.setString(4, rp.getAirportA().getIATA());
		executeUpdate(0);
	}
	
	/*
	 * Updates flight statistics for a particular Airport.
	 */
	private void updateAirport(Airport a, boolean isDeparture) throws SQLException {
		String apColumn = isDeparture ? "AIRPORT_D" : "AIRPORT_A";
		prepareStatementWithoutLimits("REPLACE INTO FLIGHTSTATS_AIRPORT (SELECT ?, COUNT(DISTANCE) AS LEGS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS ACARS, "
			+ "SUM(IF((ATTR & ?) > 0, 1, 0)) AS VATSIM, SUM(IF((ATTR & ?) > 0, 1, 0)) AS IVAO, SUM(IF((ATTR & ?) > 0, 1, 0)) AS HIST, SUM(IF((ATTR & ?) > 0, 1, 0)) AS DSP, "
			+ "SUM(DISTANCE) AS MILES, SUM(FLIGHT_TIME) AS HOURS, COUNT(DISTINCT PILOT_ID) AS PIDS, AVG(LOADFACTOR), SUM(PAX), SUM(IF(FSVERSION=?,1,0)) AS FS7, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS FS8, SUM(IF(FSVERSION=?,1,0)) AS FS9, SUM(IF(FSVERSION=?,1,0)) AS FSX, SUM(IF(FSVERSION=?,1,0)) AS P3D, "
			+ "SUM(IF(FSVERSION=?,1,IF(FSVERSION=?,1,0))) AS XP, SUM(IF(FSVERSION=0,1,0)) AS FSO, ? FROM PIREPS WHERE (STATUS=?) AND (" + apColumn + "=?))");
		_ps.setString(1, a.getIATA());
		_ps.setInt(2, FlightReport.ATTR_ACARS);
		_ps.setInt(3, FlightReport.ATTR_VATSIM);
		_ps.setInt(4, FlightReport.ATTR_IVAO);
		_ps.setInt(5, FlightReport.ATTR_HISTORIC);
		_ps.setInt(6, FlightReport.ATTR_DISPATCH);
		_ps.setInt(7, Simulator.FS2000.getCode());
		_ps.setInt(8, Simulator.FS2002.getCode());
		_ps.setInt(9, Simulator.FS9.getCode());
		_ps.setInt(10, Simulator.FSX.getCode());
		_ps.setInt(11, Simulator.P3D.getCode());
		_ps.setInt(12, Simulator.XP9.getCode());
		_ps.setInt(13, Simulator.XP10.getCode());
		_ps.setBoolean(14, isDeparture);
		_ps.setInt(15, FlightReport.OK);
		_ps.setString(16, a.getIATA());
		executeUpdate(1);
	}
	
	/*
	 * Updates flight statistics for a particular Date.
	 */
	private void updateDate(java.util.Date dt) throws SQLException {
		prepareStatementWithoutLimits("REPLACE INTO FLIGHTSTATS_DATE (SELECT DATE, COUNT(DISTANCE) AS LEGS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS ACARS, "
			+ "SUM(IF((ATTR & ?) > 0, 1, 0)) AS VATSIM, SUM(IF((ATTR & ?) > 0, 1, 0)) AS IVAO, SUM(IF((ATTR & ?) > 0, 1, 0)) AS HIST, SUM(IF((ATTR & ?) > 0, 1, 0)) AS DSP, "
			+ "SUM(DISTANCE) AS MILES, SUM(FLIGHT_TIME) AS HOURS, COUNT(DISTINCT PILOT_ID) AS PIDS, AVG(LOADFACTOR), SUM(PAX), SUM(IF(FSVERSION=?,1,0)) AS FS7, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS FS8, SUM(IF(FSVERSION=?,1,0)) AS FS9, SUM(IF(FSVERSION=?,1,0)) AS FSX, SUM(IF(FSVERSION=?,1,0)) AS P3D, "
			+ "SUM(IF(FSVERSION=?,1,IF(FSVERSION=?,1,0))) AS XP, SUM(IF(FSVERSION=0,1,0)) AS FSO FROM PIREPS WHERE (STATUS=?) AND (DATE=DATE(?)))");
		_ps.setInt(1, FlightReport.ATTR_ACARS);
		_ps.setInt(2, FlightReport.ATTR_VATSIM);
		_ps.setInt(3, FlightReport.ATTR_IVAO);
		_ps.setInt(4, FlightReport.ATTR_HISTORIC);
		_ps.setInt(5, FlightReport.ATTR_DISPATCH);
		_ps.setInt(6, Simulator.FS2000.getCode());
		_ps.setInt(7, Simulator.FS2002.getCode());
		_ps.setInt(8, Simulator.FS9.getCode());
		_ps.setInt(9, Simulator.FSX.getCode());
		_ps.setInt(10, Simulator.P3D.getCode());
		_ps.setInt(11, Simulator.XP9.getCode());
		_ps.setInt(12, Simulator.XP10.getCode());
		_ps.setInt(13, FlightReport.OK);
		_ps.setTimestamp(14, createTimestamp(dt));
		executeUpdate(1);
	}
	
	/*
	 * Updates flight statistics for a particular equipment type.
	 */
	private void updateEQ(String eqType) throws SQLException {
		prepareStatementWithoutLimits("REPLACE INTO FLIGHTSTATS_EQTYPE (SELECT EQTYPE, COUNT(DISTANCE) AS LEGS, SUM(IF((ATTR & ?) > 0, 1, 0)) AS ACARS, "
			+ "SUM(IF((ATTR & ?) > 0, 1, 0)) AS VATSIM, SUM(IF((ATTR & ?) > 0, 1, 0)) AS IVAO, SUM(IF((ATTR & ?) > 0, 1, 0)) AS HIST, SUM(IF((ATTR & ?) > 0, 1, 0)) AS DSP, "
			+ "SUM(DISTANCE) AS MILES, SUM(FLIGHT_TIME) AS HOURS, COUNT(DISTINCT PILOT_ID) AS PIDS, AVG(LOADFACTOR), SUM(PAX), SUM(IF(FSVERSION=?,1,0)) AS FS7, "
			+ "SUM(IF(FSVERSION=?,1,0)) AS FS8, SUM(IF(FSVERSION=?,1,0)) AS FS9, SUM(IF(FSVERSION=?,1,0)) AS FSX, SUM(IF(FSVERSION=?,1,0)) AS P3D, "
			+ "SUM(IF(FSVERSION=?,1,IF(FSVERSION=?,1,0))) AS XP, SUM(IF(FSVERSION=0,1,0)) AS FSO FROM PIREPS WHERE (STATUS=?) AND (EQTYPE=?))");
		_ps.setInt(1, FlightReport.ATTR_ACARS);
		_ps.setInt(2, FlightReport.ATTR_VATSIM);
		_ps.setInt(3, FlightReport.ATTR_IVAO);
		_ps.setInt(4, FlightReport.ATTR_HISTORIC);
		_ps.setInt(5, FlightReport.ATTR_DISPATCH);
		_ps.setInt(6, Simulator.FS2000.getCode());
		_ps.setInt(7, Simulator.FS2002.getCode());
		_ps.setInt(8, Simulator.FS9.getCode());
		_ps.setInt(9, Simulator.FSX.getCode());
		_ps.setInt(10, Simulator.P3D.getCode());
		_ps.setInt(11, Simulator.XP9.getCode());
		_ps.setInt(12, Simulator.XP10.getCode());
		_ps.setInt(13, FlightReport.OK);
		_ps.setString(14, eqType);
		executeUpdate(1);
	}
}