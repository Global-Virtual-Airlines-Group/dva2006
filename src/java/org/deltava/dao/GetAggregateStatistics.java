// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.stats.FlightStatsEntry;

/**
 * A Data Access Object to read aggregated Flight Report statistics. 
 * @author Luke
 * @version 6.3
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
	 * @param groupBy the &quot;GROUP BY&quot; column name
	 * @param orderBy the &quot;ORDER BY&quot; column name
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getSimStatistics(String groupBy, String orderBy) throws DAOException {
		
		// Get the SQL statement to use
		boolean isPilot = groupBy.contains("P.");
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(groupBy);
		sqlBuf.append(" AS LABEL, SUM(LEGS) AS SL, SUM(HOURS) AS SH, SUM(MILES) AS SM, SUM(FS2000), SUM(FS2002), SUM(FS2004) AS SFS9, SUM(FSX) AS SFSX, "
				+ "SUM(P3D) AS SP3D, SUM(XP10) AS SXP, SUM(OTHER_SIM) FROM ");
		if ("EQTYPE".equals(groupBy))
			sqlBuf.append("FLIGHTSTATS_EQTYPE F");
		else if (isPilot)
			sqlBuf.append("FLIGHTSTATS_PILOT F, PILOTS P WHERE (P.ID=F.PILOT_ID)");
		else
			sqlBuf.append("FLIGHTSTATS_DATE F");
		
		sqlBuf.append(" GROUP BY LABEL ORDER BY ");
		sqlBuf.append(orderBy);
		
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
					entry.setFSVersionLegs(Simulator.XP9, rs.getInt(10));
					entry.setFSVersionLegs(Simulator.UNKNOWN, rs.getInt(11));
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
	 * @param orderBy the &quot;ORDER BY&quot; column name
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getAirportStatistics(String orderBy, int apType) throws DAOException {

		// Get the SQL statement to use
		StringBuilder sqlBuf = new StringBuilder("SELECT AP.NAME, SUM(LEGS) AS SL, SUM(HOURS) AS SH, SUM(MILES) AS SM, SUM(HISTORIC) AS SHL, "
			+ "SUM(DISPATCH) AS SDL, SUM(ACARS) AS SAL, SUM(VATSIM) AS OVL, SUM(IVAO) AS OIL, SUM(FS2000), SUM(FS2002), SUM(FS2004) AS SFS9, "
			+ "SUM(FSX) AS SFSX, SUM(P3D) AS SP3D, SUM(XP10) AS SXP, SUM(OTHER_SIM), AVG(LOADFACTOR), SUM(PAX) AS SP, COUNT(DISTINCT PILOTS) AS PIDS, "
			+ "SUM(IVAO+VATSIM) AS OLEGS, SUM(MILES)/SUM(LEGS) AS AVGMILES, SUM(HOURS)/SUM(LEGS) AS AVGHOURS FROM FLIGHTSTATS_AIRPORT F, common.AIRPORTS AP "
			+ "WHERE (F.IATA=AP.IATA) ");
		if (apType == 1)
			sqlBuf.append("AND (IS_DEPARTURE=1) ");
		else if (apType == 2)
			sqlBuf.append("AND (IS_DEPARTURE=0) ");
		
		sqlBuf.append("GROUP BY AP.NAME ORDER BY ");
		sqlBuf.append(orderBy);
		
		try {
			prepareStatement(sqlBuf.toString());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves aggregated Flight Report statistics.
	 * @param groupBy the &quot;GROUP BY&quot; column name
	 * @param orderBy the &quot;ORDER BY&quot; column name
	 * @return a Collection of FlightStatsEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightStatsEntry> getPIREPStatistics(String groupBy, String orderBy) throws DAOException {

		// Get the SQL statement to use
		StringBuilder sqlBuf = new StringBuilder("SELECT ");
		sqlBuf.append(groupBy);
		sqlBuf.append(" AS LABEL, SUM(LEGS) AS SL, SUM(HOURS) AS SH, SUM(MILES) AS SM, SUM(HISTORIC) AS SHL, SUM(DISPATCH) AS SDL, "
			+ "SUM(ACARS) AS SAL, SUM(VATSIM) AS OVL, SUM(IVAO) AS OIL, SUM(FS2000), SUM(FS2002), SUM(FS2004) AS SFS9, SUM(FSX) AS SFSX, "
			+ "SUM(P3D) AS SP3D, SUM(XP10) AS SXP, SUM(OTHER_SIM), AVG(LOADFACTOR), SUM(PAX) AS SP, COUNT(DISTINCT PILOTS) AS PIDS, "
			+ "SUM(IVAO+VATSIM) AS OLEGS, SUM(MILES)/SUM(LEGS) AS AVGMILES, SUM(HOURS)/SUM(LEGS) AS AVGHOURS FROM ");
		if (groupBy.contains("P."))
			sqlBuf.append("FLIGHTSTATS_PILOT F, PILOTS P WHERE (P.ID=F.PILOT_ID)");
		else if (groupBy.contains("EQTYPE"))
			sqlBuf.append("FLIGHTSTATS_EQTYPE F");
		else
			sqlBuf.append("FLIGHTSTATS_DATE F");
		sqlBuf.append(" GROUP BY LABEL ORDER BY ");
		sqlBuf.append(orderBy);

		try {
			prepareStatement(sqlBuf.toString());
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
				entry.setFSVersionLegs(Simulator.XP9, rs.getInt(15));
				entry.setFSVersionLegs(Simulator.UNKNOWN, rs.getInt(16));
				entry.setLoadFactor(rs.getDouble(17));
				entry.setPax(rs.getInt(18));
				entry.setPilotIDs(rs.getInt(19));
				results.add(entry);
			}
		}

		_ps.close();
		return results;
	}
}