// Copyright 2020, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.flight.FlightHistoryEntry;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.flight.HistoryType;
import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object to load Flight Report status updates from the database.
 * @author Luke
 * @version 10.3
 * @since 9.0
 */

public class GetFlightReportHistory extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetFlightReportHistory(Connection c) {
		super(c);
	}

	/**
	 * Loads status updates for a particular Flight Report.
	 * @param id the Flight Report database ID
	 * @return a Collection of FlightHistoryEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<FlightHistoryEntry> getEntries(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM PIREP_STATUS_HISTORY WHERE (ID=?)")) {
			ps.setInt(1, id);
			Collection<FlightHistoryEntry> results = new ArrayList<FlightHistoryEntry>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					FlightHistoryEntry upd = new FlightHistoryEntry(id, HistoryType.values()[rs.getInt(3)], rs.getInt(2), toInstant(rs.getTimestamp(4)), rs.getString(5));
					results.add(upd);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all status updates for a Pilot's Flight Reports. 
	 * @param pilotID the Pilot's database ID
	 * @param pireps a Collection of FlightReports for this Pilot
	 * @throws DAOException if a JDBC error occurs
	 */
	public void loadStatus(int pilotID, Collection<FlightReport> pireps) throws DAOException {
		Map<Integer, FlightReport> pMap = CollectionUtils.createMap(pireps, FlightReport::getID);
		try (PreparedStatement ps = prepareWithoutLimits("SELECT PSH.* FROM PIREP_STATUS_HISTORY PSH, PIREPS P WHERE (P.ID=PSH.ID) AND (P.PILOT_ID=?)")) {
			ps.setFetchSize(Math.min(pireps.size() * 4, 1000));
			ps.setInt(1, pilotID);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					FlightReport fr = pMap.get(Integer.valueOf(rs.getInt(1)));
					if (fr != null) {
						FlightHistoryEntry upd = new FlightHistoryEntry(fr.getID(), HistoryType.values()[rs.getInt(3)], rs.getInt(2), toInstant(rs.getTimestamp(4)), rs.getString(5));
						fr.addStatusUpdate(upd);
					}
				}
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}