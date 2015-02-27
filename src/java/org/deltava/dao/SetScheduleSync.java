// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.schedule.Airline;

/**
 * A Data Access Object to synchronize flight schedules.
 * @author Luke
 * @version 6.0
 * @since 6.0
 */

public class SetScheduleSync extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetScheduleSync(Connection c) {
		super(c);
	}

	/**
	 * Copies all schedule entries for a particular airline into the current database.
	 * @param al the Airline
	 * @param srcDB the source database name
	 * @return the number of entries copied
	 * @throws DAOException if a JDBC error occurs
	 */
	public int copy(Airline al, String srcDB) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("INSERT INTO SCHEDULE (SELECT * FROM ");
		sqlBuf.append(formatDBName(srcDB));
		sqlBuf.append(".SCHEDULE WHERE (AIRLINE=?))");
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, al.getCode());
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Purges schedule entries from a particular airline from the current database. <i>This will
	 * remove all entries whether they are purgeable or not.</i>
	 * @param al the Airline
	 * @return the number of schedule entries deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purge(Airline al) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM SCHEDULE WHERE (AIRLINE=?)");
			_ps.setString(1, al.getCode());
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}		
	}	
}