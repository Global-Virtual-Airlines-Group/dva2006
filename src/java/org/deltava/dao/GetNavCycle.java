// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

/**
 * A Data Access Object to load chart/navigation data cycle update dates. 
 * @author Luke
 * @version 5.1
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
	 * Returns the chart/navigation data cycle for a particular date.
	 * @param dt the date
	 * @return the Cycle ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public String getCycle(java.util.Date dt) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT ID FROM common.NAVCYCLE WHERE (RELEASED<?) ORDER BY RELEASED DESC LIMIT 1");
			_ps.setTimestamp(1, createTimestamp(dt));
			
			String cycle = null;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					cycle = rs.getString(1);
			}
			
			_ps.close();
			return cycle;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}