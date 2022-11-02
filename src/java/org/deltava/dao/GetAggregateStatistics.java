// Copyright 2015, 2016, 2017, 2019, 2020, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.stats.*;

/**
 * A Data Access Object to read aggregated Flight Report statistics. 
 * @author Luke
 * @version 10.3
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
		sqlBuf.append(" AS LABEL, SUM(LEGS) AS SL, SUM(HOURS) AS SH, SUM(MILES) AS SM, SUM(FS2000), SUM(FS2002), SUM(FS2004) AS SFS9, SUM(FSX) AS SFSX, SUM(P3D) AS SP3D, "
			+ "SUM(P3Dv4) AS SP3DV4, SUM(XP10) AS SXP, SUM(XP11) AS SXP11, SUM(XP12) AS SXP12, SUM(FS20) AS SMSFS, SUM(OTHER_SIM), SUM(PAX) AS PAX, AVG(LOADFACTOR) AS LF, "
			+ "AVG(MILES) AS AVGMILES, AVG(HOURS) AS AVGHOURS FROM ");
		if ("F.EQTYPE".equals(grp.getSQL()))
			sqlBuf.append("FLIGHTSTATS_EQTYPE F");
		else if (grp.isPilotGroup())
			sqlBuf.append("FLIGHTSTATS_PILOT F, PILOTS P WHERE (P.ID=F.PILOT_ID)");
		else
			sqlBuf.append("FLIGHTSTATS_DATE F");
		
		sqlBuf.append(" GROUP BY LABEL ORDER BY ");
		sqlBuf.append(s.getSQL());
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			Collection<FlightStatsEntry> results = new ArrayList<FlightStatsEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					FlightStatsEntry entry = new FlightStatsEntry(rs.getString(1), rs.getInt(2), rs.getDouble(3), rs.getInt(4));
					entry.setFSVersionLegs(Simulator.FS2000, rs.getInt(5));
					entry.setFSVersionLegs(Simulator.FS2002, rs.getInt(6));
					entry.setFSVersionLegs(Simulator.FS9, rs.getInt(7));
					entry.setFSVersionLegs(Simulator.FSX, rs.getInt(8));
					entry.setFSVersionLegs(Simulator.P3D, rs.getInt(9));
					entry.setFSVersionLegs(Simulator.P3Dv4, rs.getInt(10));
					entry.setFSVersionLegs(Simulator.XP10, rs.getInt(11));
					entry.setFSVersionLegs(Simulator.XP11, rs.getInt(12));
					entry.setFSVersionLegs(Simulator.XP12, rs.getInt(13));
					entry.setFSVersionLegs(Simulator.FS2020, rs.getInt(14));
					entry.setFSVersionLegs(Simulator.UNKNOWN, rs.getInt(15));
					entry.setPax(rs.getInt(16));
					entry.setLoadFactor(rs.getDouble(17));
					results.add(entry);
				}
			}

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
			+ "SUM(SIMBRIEF) AS SBL, SUM(ACARS) AS SAL, SUM(VATSIM) AS OVL, SUM(IVAO) AS OIL, SUM(FS2000), SUM(FS2002), SUM(FS2004) AS SFS9, SUM(FSX) AS SFSX, SUM(P3D) AS SP3D, "
			+ "SUM(P3Dv4) AS SP3Dv4, SUM(XP10) AS SXP, SUM(XP11) AS SXP11, SUM(XP12) AS SXP12, SUM(FS20) AS SMSFS, SUM(OTHER_SIM), SUM(PAX) AS SP, AVG(LOADFACTOR) AS LF, "
			+ "SUM(PILOTS) AS PIDS, SUM(IVAO+VATSIM) AS OLEGS, SUM(MILES)/SUM(LEGS) AS AVGMILES, SUM(HOURS)/SUM(LEGS) AS AVGHOURS FROM FLIGHTSTATS_AIRPORT F, "
			+ "common.AIRPORTS AP WHERE (F.IATA=AP.IATA) ");
		if (apType == 1)
			sqlBuf.append("AND (IS_DEPARTURE=1) ");
		else if (apType == 2)
			sqlBuf.append("AND (IS_DEPARTURE=0) ");
		
		sqlBuf.append("GROUP BY AP.NAME ORDER BY ");
		sqlBuf.append(s.getSQL());
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns online network flight statstics by date.
	 * @param srt
	 * @param grp
	 * @return a Collection of OnlineStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<OnlineStatsEntry> getOnlineStatistics(FlightStatsSort srt, FlightStatsGroup grp) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(grp.getSQL());
		sqlBuf.append(" AS LABEL, SUM(F.LEGS) AS TOTAL, SUM(IF(F.NETWORK=?,F.LEGS,0)) AS VL, SUM(IF(F.NETWORK=?,F.HOURS, 0)) AS VH, SUM(IF(F.NETWORK=?,F.LEGS,0)) AS IL, SUM(IF(F.NETWORK=?,F.HOURS, 0)) AS IH, "
			+ "SUM(IF(F.NETWORK=?,F.LEGS,0)) AS PEL, SUM(IF(F.NETWORK=?,F.HOURS, 0)) AS PEH, SUM(IF(F.NETWORK=?,F.LEGS,0)) AS PSL, SUM(IF(F.NETWORK=?,F.HOURS, 0)) AS PSH FROM FLIGHTSTATS_NETWORK F GROUP BY LABEL ORDER BY ");
		sqlBuf.append(srt.getSQL());
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setInt(1, OnlineNetwork.VATSIM.ordinal());
			ps.setInt(2, OnlineNetwork.VATSIM.ordinal());
			ps.setInt(3, OnlineNetwork.IVAO.ordinal());
			ps.setInt(4, OnlineNetwork.IVAO.ordinal());
			ps.setInt(5, OnlineNetwork.PILOTEDGE.ordinal());
			ps.setInt(6, OnlineNetwork.PILOTEDGE.ordinal());
			ps.setInt(7, OnlineNetwork.POSCON.ordinal());
			ps.setInt(8, OnlineNetwork.POSCON.ordinal());
			
			Collection<OnlineStatsEntry> results = new ArrayList<OnlineStatsEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					OnlineStatsEntry st = new OnlineStatsEntry(rs.getString(1));
					st.setTotalLegs(rs.getInt(2));
					st.setNetwork(OnlineNetwork.VATSIM, rs.getInt(3), rs.getDouble(4));
					st.setNetwork(OnlineNetwork.IVAO, rs.getInt(5), rs.getDouble(6));
					st.setNetwork(OnlineNetwork.PILOTEDGE, rs.getInt(7), rs.getDouble(8));
					st.setNetwork(OnlineNetwork.POSCON, rs.getInt(9), rs.getDouble(10));
					results.add(st);
				}
			}
			
			return results;
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
		sqlBuf.append(" AS LABEL, SUM(LEGS) AS SL, SUM(HOURS) AS SH, SUM(MILES) AS SM, SUM(HISTORIC) AS SHL, SUM(DISPATCH) AS SDL, SUM(SIMBRIEF) AS SBL, "
			+ "SUM(ACARS) AS SAL, SUM(VATSIM) AS OVL, SUM(IVAO) AS OIL, SUM(FS2000), SUM(FS2002), SUM(FS2004) AS SFS9, SUM(FSX) AS SFSX, SUM(P3D) AS SP3D, "
			+ "SUM(P3Dv4) AS SP3DV4, SUM(XP10) AS SXP, SUM(XP11) AS SXP11, SUM(XP12) AS SXP12, SUM(FS20) AS SMSFS, SUM(OTHER_SIM), SUM(PAX) AS SP, "
			+ "AVG(LOADFACTOR) AS LF, ");
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

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			if (grp.isAirportGroup() && (grp != FlightStatsGroup.AP))
				ps.setBoolean(1, (grp == FlightStatsGroup.AD));
			
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse stats entry result sets.
	 */
	private static List<FlightStatsEntry> execute(PreparedStatement ps) throws SQLException {
		try (ResultSet rs = ps.executeQuery()) {
			List<FlightStatsEntry> results = new ArrayList<FlightStatsEntry>();
			while (rs.next()) {
				FlightStatsEntry entry = new FlightStatsEntry(rs.getString(1), rs.getInt(2), rs.getDouble(3), rs.getInt(4));
				entry.setHistoricLegs(rs.getInt(5));
				entry.setDispatchLegs(rs.getInt(6));
				entry.setSimBriefLegs(rs.getInt(7));
				entry.setACARSLegs(rs.getInt(8));
				entry.setOnlineLegs(rs.getInt(9) + rs.getInt(10));
				entry.setFSVersionLegs(Simulator.FS2000, rs.getInt(11));
				entry.setFSVersionLegs(Simulator.FS2002, rs.getInt(12));
				entry.setFSVersionLegs(Simulator.FS9, rs.getInt(13));
				entry.setFSVersionLegs(Simulator.FSX, rs.getInt(14));
				entry.setFSVersionLegs(Simulator.P3D, rs.getInt(15));
				entry.setFSVersionLegs(Simulator.P3Dv4, rs.getInt(16));
				entry.setFSVersionLegs(Simulator.XP10, rs.getInt(17));
				entry.setFSVersionLegs(Simulator.XP11, rs.getInt(18));
				entry.setFSVersionLegs(Simulator.XP12, rs.getInt(19));
				entry.setFSVersionLegs(Simulator.FS2020, rs.getInt(20));
				entry.setFSVersionLegs(Simulator.UNKNOWN, rs.getInt(21));
				entry.setPax(rs.getInt(22));
				entry.setLoadFactor(rs.getDouble(23));
				entry.setPilotIDs(rs.getInt(24));
				results.add(entry);
			}
			
			return results;
		}
	}
}