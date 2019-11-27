// Copyright 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.navdata.RunwayMapping;

/**
 * A Data Access Object to update runway mappings in the database. 
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.RUNWAY_RENUMBER (ICAO, OLDCODE, NEWCODE) VALUES (?, ?, ?)")) {
			ps.setString(1, rm.getICAO());
			ps.setString(2, rm.getOldCode());
			ps.setString(3, rm.getNewCode());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.RUNWAY_RENUMBER WHERE (ICAO=?)")) {
			ps.setString(1, icao);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}