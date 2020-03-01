// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.flight.FlightHistoryEntry;
import org.deltava.beans.flight.HistoryType;

/**
 * A Data Access Object to load Flight Report status updates from the database.
 * @author Luke
 * @version 9.0
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
}