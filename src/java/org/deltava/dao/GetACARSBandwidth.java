// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.Bandwidth;

/**
 * A Data Access Object to load ACARS bandwidth statistics. 
 * @author Luke
 * @version 2.2
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
		try {
			prepareStatementWithoutLimits("SELECT * FROM acars.BANDWIDTH WHERE (DURATION=?) "
					+ "ORDER BY PERIOD DESC LIMIT 1");
			_ps.setInt(1, 1);
			List<Bandwidth> results = execute();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves bandwidth stats that have not yet been aggregated.
	 * @return a Collection of Bandwidth beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Bandwidth> getRaw() throws DAOException {
		try {
			prepareStatement("SELECT * FROM acars.BANDWIDTH WHERE (DURATION=?) ORDER BY "
					+ "PERIOD DESC");
			_ps.setInt(1, 1);
			return execute();
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
		try {
			prepareStatement("SELECT * FROM acars.BANDWIDTH WHERE (DURATION=?) ORDER BY "
					+ "PERIOD DESC");
			_ps.setInt(1, 60);
			return execute();
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
		try {
			prepareStatement("SELECT DATE(PERIOD) AS DT, 1440, AVG(CONS), SUM(BYTES_IN), "
					+ "SUM(BYTES_OUT), SUM(MSGS_IN), SUM(MSGS_OUT), MAX(PEAK_CONS), MAX(PEAK_BYTES), "
					+ "MAX(PEAK_MSGS) FROM acars.BANDWIDTH GROUP BY DT DESC");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse result sets.
	 */
	private List<Bandwidth> execute() throws SQLException {
		List<Bandwidth> results = new ArrayList<Bandwidth>();
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Bandwidth bw = new Bandwidth(rs.getTimestamp(1));
			bw.setInterval(rs.getInt(2));
			bw.setConnections(rs.getInt(3));
			bw.setBytes(rs.getLong(4), rs.getLong(5));
			bw.setMessages(rs.getInt(6), rs.getInt(7));
			bw.setMaxConnections(rs.getInt(8));
			bw.setMaxBytes(rs.getLong(9));
			bw.setMaxMsgs(rs.getInt(10));
			results.add(bw);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}