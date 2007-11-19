//Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.schedule.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object for Approach Charts.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetChart extends DAO {

	/**
	 * Creates the DAO with a JDBC connection.
	 * @param c the JDBC connection to use
	 */
	public GetChart(Connection c) {
		super(c);
	}

	/**
	 * Returns all Airports with available charts.
	 * @return a List of Airports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Airport> getAirports() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT C.ICAO FROM common.CHARTS C, common.AIRPORTS A "
					+ "WHERE (C.ICAO=A.ICAO) ORDER BY A.NAME");

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			List<Airport> results = new ArrayList<Airport>();
			while (rs.next()) {
				Airport a = SystemData.getAirport(rs.getString(1));
				if (a != null)
					results.add(a);
			}

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Chart metadata for a particular Airport.
	 * @param a the Airport bean
	 * @return a List of Chart objects
	 * @throws DAOException if a JDBC error occurs
	 * @see GetChart#getCharts(Airport)
	 */
	public List<Chart> getCharts(Airport a) throws DAOException {
		try {
			// Prepare the statement
			prepareStatement("SELECT * FROM common.CHARTS WHERE (ICAO=?) ORDER BY NAME");
			_ps.setString(1, a.getICAO());

			// Execute the query
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Charts for a particular Event.
	 * @param eventID the event Database ID
	 * @return a List of Chart objects
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Chart> getChartsByEvent(int eventID) throws DAOException {
		try {
			prepareStatement("SELECT C.* FROM common.CHARTS C, events.EVENT_CHARTS EC WHERE "
					+ "(EC.ID=?) AND (C.ID=EC.CHART) ORDER BY C.NAME");
			_ps.setInt(1, eventID);

			// Execute the query
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns a Chart based on its Database ID.
	 * @param id the database id
	 * @return the Chart
	 * @throws DAOException if a JDBC error occurs
	 */
	public Chart get(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM common.CHARTS WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);

			// Execute the query
			List<Chart> results = execute();
			return (results.isEmpty()) ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves Charts based on a group of database IDs.
	 * @param IDs a Collection of database IDs as Integers
	 * @return a Collection of Charts
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Chart> getByIDs(Collection<Integer> IDs) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM common.CHARTS WHERE (ID IN (");
		for (Iterator<Integer> i = IDs.iterator(); i.hasNext();) {
			Integer id = i.next();
			sqlBuf.append(String.valueOf(id));
			if (i.hasNext())
				sqlBuf.append(',');
		}

		// Clear off the trailing comma
		sqlBuf.append("))");
		setQueryMax(IDs.size());

		// Load from the database
		try {
			prepareStatement(sqlBuf.toString());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to load chart metadata.
	 */
	private List<Chart> execute() throws SQLException {
		List<Chart> results = new ArrayList<Chart>();

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Chart c = new Chart(rs.getString(5), SystemData.getAirport(rs.getString(2)));
			c.setID(rs.getInt(1));
			c.setType(rs.getInt(3));
			c.setImgType(rs.getInt(4));
			c.setSize(rs.getInt(6));
			results.add(c);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}