// Copyright 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.navdata.RunwayMapping;

/**
 * A Data Access Object to update runway mappings in the database. 
 * @author Luke
 * @version 8.5
 * @since 8.3
 */

public class SetRunwayMapping extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetRunwayMapping(Connection c) {
		super(c);
	}

	/**
	 * Writes a RunwayMapping bean to the database.
	 * @param rm a RunwayMapping
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(RunwayMapping rm) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO common.RUNWAY_RENUMBER (ICAO, OLDCODE, NEWCODE) VALUES (?, ?, ?)");
			_ps.setString(1, rm.getICAO());
			_ps.setString(2, rm.getOldCode());
			_ps.setString(3, rm.getNewCode());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Clears all Runway mappings for a particular Airport from the database. 
	 * @param icao the ICAO code
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clear(String icao) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.RUNWAY_RENUMBER WHERE (ICAO=?)");
			_ps.setString(1, icao);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}