//Copyright 2005, 2006, 2007, 2011, 2012, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.schedule.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object for Approach Charts.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT C.ICAO FROM common.CHARTS C, common.AIRPORTS A WHERE (C.ICAO=A.ICAO) ORDER BY A.NAME")) {
			List<Airport> results = new ArrayList<Airport>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Airport a = SystemData.getAirport(rs.getString(1));
					if (a != null)
						results.add(a);
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the average age of charts.
	 * @return a Map of maximum ages in days, keyed by Airport
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Airport, Integer> getMaxAges() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT ICAO, IFNULL(MAX(TIMESTAMPDIFF(DAY, LASTMODIFIED, NOW())), -1) AS AGE FROM common.CHARTS GROUP BY ICAO")) {
			Map<Airport, Integer> results = new LinkedHashMap<Airport, Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Airport a = SystemData.getAirport(rs.getString(1));
					if (a != null)
						results.put(a,  Integer.valueOf(rs.getInt(2)));
				}
			}

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
		try (PreparedStatement ps = prepare("SELECT C.*, CU.SOURCE, CU.URL, CU.EXTERNAL_ID FROM common.CHARTS C LEFT JOIN common.CHARTURLS CU ON (C.ID=CU.ID) WHERE (C.ICAO=?) ORDER BY C.NAME")) {
			ps.setString(1, a.getICAO());
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT C.*, CU.SOURCE, CU.URL, CU.EXTERNAL_ID FROM events.EVENT_CHARTS EC, common.CHARTS C LEFT JOIN common.CHARTURLS CU ON (C.ID=CU.ID) WHERE (EC.ID=?) AND (C.ID=EC.CHART) ORDER BY C.NAME")) {
			ps.setInt(1, eventID);
			return execute(ps);
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
		try (PreparedStatement ps = prepareWithoutLimits("SELECT C.*, CU.SOURCE, CU.URL, CU.EXTERNAL_ID FROM common.CHARTS C LEFT JOIN common.CHARTURLS CU ON (C.ID=CU.ID) WHERE (C.ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			return execute(ps).stream().findFirst().orElse(null);
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
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, CU.SOURCE, CU.URL, CU.EXTERNAL_ID FROM common.CHARTS C LEFT JOIN common.CHARTURLS CU ON (C.ID=CU.ID) WHERE (C.ID IN (");
		for (Iterator<Integer> i = IDs.iterator(); i.hasNext();) {
			Integer id = i.next();
			sqlBuf.append(String.valueOf(id));
			if (i.hasNext())
				sqlBuf.append(',');
		}

		sqlBuf.append("))");
		setQueryMax(IDs.size());

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the database IDs for a set of external Chart IDs.
	 * @param externalIDs a Collection of external chart IDs
	 * @return a Map of database IDs, keyed by external ID
	 * @throws DAOException
	 * @see ExternalChart#getExternalID()
	 */
	public Map<String, Integer> getChartIDs(Collection<String> externalIDs) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT ID FROM common.CHARTURLS WHERE (EXTERNAL_ID=?)")) {
			Map<String, Integer> results = new HashMap<String, Integer>();
			for (String id : externalIDs) {
				ps.setString(1, id);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next())
						results.put(id, Integer.valueOf(rs.getInt(1)));
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to load chart metadata.
	 */
	private static List<Chart> execute(PreparedStatement ps) throws SQLException {
		List<Chart> results = new ArrayList<Chart>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean hasExt = (rs.getMetaData().getColumnCount() > 10);
			while (rs.next()) {
				String url = hasExt ? rs.getString(11) : null;
				Chart c = null;
				if (url != null) {
					ExternalChart ec = new ExternalChart(rs.getString(5), SystemData.getAirport(rs.getString(2)));
					ec.setSource(rs.getString(10));
					ec.setExternalID(rs.getString(12));
					ec.setURL(url);
					c = ec;
				} else
					c = new Chart(rs.getString(5), SystemData.getAirport(rs.getString(2)));
			
				c.setID(rs.getInt(1));
				c.setType(Chart.Type.values()[rs.getInt(3)]);
				c.setImgType(Chart.ImageType.values()[rs.getInt(4)]);
				c.setSize(rs.getInt(6));
				c.setUseCount(rs.getInt(7));
				c.setLastModified(toInstant(rs.getTimestamp(8)));
				results.add(c);
			}
		}

		return results;
	}
}