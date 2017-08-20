// Copyright 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.stats.*;

/**
 * A Data Access Object to read aggregated Flight Report statistics. 
 * @author Luke
 * @version 7.5
 * @since 6.2
 */

public class GetAggregateStatistics extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAggregateStatistics(Connection c) {
		super(c);
	}

	/**
	 * Retrieves aggregated approved Flight Report statistics.
	 * @param s the statistics sorting option
	 * @param grp the statistics grouping option
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getSimStatistics(FlightStatsSort s, FlightStatsGroup grp) throws DAOException {
		
		// Get the SQL statement to use
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(grp.getSQL());
		sqlBuf.append(" AS LABEL, SUM(LEGS) AS SL, SUM(HOURS) AS SH, SUM(MILES) AS SM, SUM(FS2000), SUM(FS2002), SUM(FS2004) AS SFS9, SUM(FSX) AS SFSX, "
				+ "SUM(P3D) AS SP3D, SUM(P3Dv4) AS SP3DV4, SUM(XP10) AS SXP, SUM(OTHER_SIM), SUM(PAX) AS PAX, AVG(LOADFACTOR) AS LF, AVG(MILES) AS AVGMILES, "
				+ "AVG(HOURS) AS AVGHOURS FROM ");
		if ("F.EQTYPE".equals(grp.getSQL()))
			sqlBuf.append("FLIGHTSTATS_EQTYPE F");
		else if (grp.isPilotGroup())
			sqlBuf.append("FLIGHTSTATS_PILOT F, PILOTS P WHERE (P.ID=F.PILOT_ID)");
		else
			sqlBuf.append("FLIGHTSTATS_DATE F");
		
		sqlBuf.append(" GROUP BY LABEL ORDER BY ");
		sqlBuf.append(s.getSQL());
		
		try {
			prepareStatement(sqlBuf.toString());
			Collection<FlightStatsEntry> results = new ArrayList<FlightStatsEntry>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					FlightStatsEntry entry = new FlightStatsEntry(rs.getString(1), rs.getInt(2), rs.getDouble(3), rs.getInt(4));
					entry.setFSVersionLegs(Simulator.FS2000, rs.getInt(5));
					entry.setFSVersionLegs(Simulator.FS2002, rs.getInt(6));
					entry.setFSVersionLegs(Simulator.FS9, rs.getInt(7));
					entry.setFSVersionLegs(Simulator.FSX, rs.getInt(8));
					entry.setFSVersionLegs(Simulator.P3D, rs.getInt(9));
					entry.setFSVersionLegs(Simulator.P3Dv4, rs.getInt(10));
					entry.setFSVersionLegs(Simulator.XP9, rs.getInt(11));
					entry.setFSVersionLegs(Simulator.UNKNOWN, rs.getInt(12));
					entry.setPax(rs.getInt(13));
					entry.setLoadFactor(rs.getDouble(14));
					results.add(entry);
				}
			}

			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves aggregated approved Flight Report statistics.
	 * @param s the sorting option
	 * @param apType the airport type, 1 for departure and 2 for arrival, 0 for all
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getAirportStatistics(FlightStatsSort s, int apType) throws DAOException {

		// Get the SQL statement to use
		StringBuilder sqlBuf = new StringBuilder("SELECT AP.NAME, SUM(LEGS) AS SL, SUM(HOURS) AS SH, SUM(MILES) AS SM, SUM(HISTORIC) AS SHL, SUM(DISPATCH) AS SDL, "
			+ "SUM(ACARS) AS SAL, SUM(VATSIM) AS OVL, SUM(IVAO) AS OIL, SUM(FS2000), SUM(FS2002), SUM(FS2004) AS SFS9, SUM(FSX) AS SFSX, SUM(P3D) AS SP3D, "
			+ "SUM(P3Dv4) AS SP3Dv4, SUM(XP10) AS SXP, SUM(OTHER_SIM), SUM(PAX) AS SP, AVG(LOADFACTOR) AS LF, SUM(PILOTS) AS PIDS, SUM(IVAO+VATSIM) AS OLEGS, "
			+ "SUM(MILES)/SUM(LEGS) AS AVGMILES, SUM(HOURS)/SUM(LEGS) AS AVGHOURS FROM FLIGHTSTATS_AIRPORT F, common.AIRPORTS AP WHERE (F.IATA=AP.IATA) ");
		if (apType == 1)
			sqlBuf.append("AND (IS_DEPARTURE=1) ");
		else if (apType == 2)
			sqlBuf.append("AND (IS_DEPARTURE=0) ");
		
		sqlBuf.append("GROUP BY AP.NAME ORDER BY ");
		sqlBuf.append(s.getSQL());
		
		try {
			prepareStatement(sqlBuf.toString());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves aggregated Flight Report statistics.
	 * @param s the statistics sorting option
	 * @param grp the statistics grouping option
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getPIREPStatistics(FlightStatsSort s, FlightStatsGroup grp) throws DAOException {

		// Get the SQL statement to use
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(grp.getSQL());
		sqlBuf.append(" AS LABEL, SUM(LEGS) AS SL, SUM(HOURS) AS SH, SUM(MILES) AS SM, SUM(HISTORIC) AS SHL, SUM(DISPATCH) AS SDL, "
			+ "SUM(ACARS) AS SAL, SUM(VATSIM) AS OVL, SUM(IVAO) AS OIL, SUM(FS2000), SUM(FS2002), SUM(FS2004) AS SFS9, SUM(FSX) AS SFSX, "
			+ "SUM(P3D) AS SP3D, SUM(P3Dv4) AS SP3DV4, SUM(XP10) AS SXP, SUM(OTHER_SIM), SUM(PAX) AS SP, AVG(LOADFACTOR) AS LF, ");
		if (grp.isDateGroup() && (grp != FlightStatsGroup.DATE))
			sqlBuf.append("0 AS PIDS");
		else
			sqlBuf.append("SUM(PILOTS) AS PIDS");
		
		sqlBuf.append(", SUM(IVAO+VATSIM) AS OLEGS, SUM(MILES)/SUM(LEGS) AS AVGMILES, SUM(HOURS)/SUM(LEGS) AS AVGHOURS FROM ");
		if (grp.isPilotGroup())
			sqlBuf.append("FLIGHTSTATS_PILOT F LEFT JOIN PILOTS P ON (F.PILOT_ID=P.ID)");
		else if (grp.getSQL().contains("EQTYPE"))
			sqlBuf.append("FLIGHTSTATS_EQTYPE F");
		else if (grp.isAirportGroup() && (grp != FlightStatsGroup.AP))
			sqlBuf.append("FLIGHTSTATS_AIRPORT FA, common.AIRPORTS AP WHERE (AP.IATA=FA.IATA) AND (FA.IS_DEPARTURE=?)");
		else if (grp.isAirportGroup())
			sqlBuf.append("FLIGHTSTATS_AIRPORT FA, common.AIRPORTS AP WHERE (AP.IATA=FA.IATA)");
		else
			sqlBuf.append("FLIGHTSTATS_DATE F");
		sqlBuf.append(" GROUP BY LABEL ORDER BY ");
		sqlBuf.append(s.getSQL());

		try {
			prepareStatement(sqlBuf.toString());
			if (grp.isAirportGroup() && (grp != FlightStatsGroup.AP))
				_ps.setBoolean(1, (grp == FlightStatsGroup.AD));
			
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse stats entry result sets.
	 */
	private List<FlightStatsEntry> execute() throws SQLException {
		List<FlightStatsEntry> results = new ArrayList<FlightStatsEntry>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				FlightStatsEntry entry = new FlightStatsEntry(rs.getString(1), rs.getInt(2), rs.getDouble(3), rs.getInt(4));
				entry.setHistoricLegs(rs.getInt(5));
				entry.setDispatchLegs(rs.getInt(6));
				entry.setACARSLegs(rs.getInt(7));
				entry.setVATSIMLegs(rs.getInt(8));
				entry.setIVAOLegs(rs.getInt(9));
				entry.setFSVersionLegs(Simulator.FS2000, rs.getInt(10));
				entry.setFSVersionLegs(Simulator.FS2002, rs.getInt(11));
				entry.setFSVersionLegs(Simulator.FS9, rs.getInt(12));
				entry.setFSVersionLegs(Simulator.FSX, rs.getInt(13));
				entry.setFSVersionLegs(Simulator.P3D, rs.getInt(14));
				entry.setFSVersionLegs(Simulator.P3Dv4, rs.getInt(15));
				entry.setFSVersionLegs(Simulator.XP9, rs.getInt(16));
				entry.setFSVersionLegs(Simulator.UNKNOWN, rs.getInt(17));
				entry.setPax(rs.getInt(18));
				entry.setLoadFactor(rs.getDouble(19));
				entry.setPilotIDs(rs.getInt(20));
				results.add(entry);
			}
		}

		_ps.close();
		return results;
	}
}