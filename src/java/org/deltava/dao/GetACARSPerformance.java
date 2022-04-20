// Copyright 2019, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.*;

/**
 * A Data Access Object to load ACARS client performance counter data from the database.
 * @author Luke
 * @version 10.2
 * @since 8.6
 */

public class GetACARSPerformance extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSPerformance(Connection c) {
		super(c);
	}

	/**
	 * Retrieves timer data for a given flight.
	 * @param flightID the ACARS Flight ID
	 * @return a Collection of TaskTimerData beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<TaskTimerData> getTimers(int flightID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM acars.PERFINFO WHERE (ID=?)")) {
			ps.setInt(1, flightID);
			
			Collection<TaskTimerData> results = new ArrayList<TaskTimerData>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					TaskTimerData ttd = new TaskTimerData(rs.getString(2), rs.getInt(3));
					ttd.setCount(rs.getLong(4));
					ttd.setTotal(rs.getLong(5));
					ttd.setMin(rs.getInt(6));
					ttd.setMax(rs.getInt(7));
					ttd.setStdDev(rs.getDouble(8));
					results.add(ttd);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves frame rate data for a given flight.
	 * @param flightID the ACARS Flight ID
	 * @return a FrameRates bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FrameRates getFrames(int flightID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM acars.FRAMERATES WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, flightID);
			
			FrameRates fr = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					fr = new FrameRates();
					fr.setID(flightID);
					fr.setSize(rs.getInt(2));
					fr.setMin(rs.getInt(3));
					fr.setMax(rs.getInt(4));
					fr.setPercentile(1, rs.getInt(5));
					fr.setPercentile(5, rs.getInt(6));
					fr.setPercentile(50, rs.getInt(7));
					fr.setPercentile(95, rs.getInt(8));
					fr.setPercentile(99, rs.getInt(9));
					fr.setAverage(rs.getDouble(10));
				}
			}
			
			return fr;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}