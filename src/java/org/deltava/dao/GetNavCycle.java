// Copyright 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.navdata.CycleInfo;

/**
 * A Data Access Object to load chart/navigation data cycle update dates. 
 * @author Luke
 * @version 7.0
 * @since 5.1
 */

public class GetNavCycle extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetNavCycle(Connection c) {
		super(c);
	}
	
	/**
	 * Returns information about a particular navigation data cycle.
	 * @param id the Cycle ID
	 * @return a CycleInfo bean, or null if not found
	 * @throws DAOException if a JDBC erorr occurs
	 */
	public CycleInfo getCycle(String id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT ID, RELEASED FROM common.NAVCYCLE WHERE (ID=?) LIMIT 1");
			_ps.setString(1, id);
			
			CycleInfo cycle = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					cycle = new CycleInfo(rs.getString(1), rs.getTimestamp(2).toInstant());
			}

			_ps.close();
			return cycle;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the chart/navigation data cycle for a particular date.
	 * @param dt the date
	 * @return the CycleInfo
	 * @throws DAOException if a JDBC error occurs
	 */
	public CycleInfo getCycle(java.time.Instant dt) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT ID, RELEASED FROM common.NAVCYCLE WHERE (RELEASED<?) ORDER BY RELEASED DESC LIMIT 1");
			_ps.setTimestamp(1, createTimestamp(dt));
			
			CycleInfo cycle = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					cycle = new CycleInfo(rs.getString(1), rs.getTimestamp(2).toInstant());
			}
			
			_ps.close();
			return cycle;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all future navigation cycle release dates.
	 * @return a Collection of release dates, ordered by date
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<CycleInfo> getFuture() throws DAOException {
		try {
			prepareStatement("SELECT ID, RELEASED FROM common.NAVCYCLE WHERE (RELEASED>=CURDATE()) ORDER BY RELEASED");
			Collection<CycleInfo> results = new ArrayList<CycleInfo>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(new CycleInfo(rs.getString(1), rs.getTimestamp(2).toInstant()));
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}