// Copyright 2008, 2010, 2011, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.Bandwidth;

/**
 * A Data Access Object to load ACARS bandwidth statistics. 
 * @author Luke
 * @version 9.0
 * @since 2.1
 */

public class GetACARSBandwidth extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSBandwidth(Connection c) {
		super(c);
	}

	/**
	 * Returns the latest bandwidth data. 
	 * @return a Bandwidth bean, or null if no data
	 * @throws DAOException if a JDBC error occurs
	 */
	public Bandwidth getLatest() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM acars.BANDWIDTH WHERE (DURATION=?) ORDER BY PERIOD DESC LIMIT 1")) {
			ps.setInt(1, 1);
			return execute(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves hourly bandwidth stats.
	 * @return a Collection of Bandwidth beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Bandwidth> getHourly() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM acars.BANDWIDTH WHERE (DURATION=?) ORDER BY PERIOD DESC")) {
			ps.setInt(1, 60);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves daily bandwidth stats.
	 * @return a Collection of Bandwidth beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Bandwidth> getDaily() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT DATE(PERIOD) AS DT, 1440, AVG(CONS), SUM(BYTES_IN), SUM(BYTES_OUT), SUM(MSGS_IN), SUM(MSGS_OUT), "
			+ "MAX(PEAK_CONS), MAX(PEAK_BYTES), MAX(PEAK_MSGS), SUM(ERRORS), SUM(BYTES_SAVED) FROM acars.BANDWIDTH GROUP BY DT DESC")) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse result sets.
	 */
	private static List<Bandwidth> execute(PreparedStatement ps) throws SQLException {
		List<Bandwidth> results = new ArrayList<Bandwidth>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Bandwidth bw = new Bandwidth(toInstant(rs.getTimestamp(1)));
				bw.setInterval(rs.getInt(2));
				bw.setConnections(rs.getInt(3));
				bw.setBytes(rs.getLong(4), rs.getLong(5));
				bw.setMessages(rs.getInt(6), rs.getInt(7));
				bw.setMaxConnections(rs.getInt(8));
				bw.setMaxBytes(rs.getLong(9));
				bw.setMaxMsgs(rs.getInt(10));
				bw.setErrors(rs.getInt(11));
				bw.setBytesSaved(rs.getLong(12));
				results.add(bw);
			}
		}
		
		return results;
	}
}