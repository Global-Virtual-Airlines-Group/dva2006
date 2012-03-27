// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.RouteEntry;

/**
 * A Data Access Object to write to the ACARS position archive.
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public class SetACARSArchive extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetACARSArchive(Connection c) {
		super(c);
	}

	/**
	 * Marks ACARS data as archived.
	 * @param flightID the ACARS Flight ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void archive(int flightID, Collection<? extends RouteEntry> positions) throws DAOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream(32768);
			try (ObjectOutputStream oo = new ObjectOutputStream(out)) {
				oo.writeInt(flightID);
				oo.writeInt(positions.size());
				for (RouteEntry re : positions)
					oo.writeObject(re);
			}
			
			startTransaction();
			
			// Write serialized data
			if (positions.size() > 0) {
				prepareStatementWithoutLimits("INSERT INTO acars.POS_ARCHIVE (ID, ARCHIVED, DATA) VALUES (?, NOW(), ?)");
				_ps.setInt(1, flightID);
				_ps.setBytes(2, out.toByteArray());
				executeUpdate(1);
			}
			
			// Delete the existing flight data
			prepareStatementWithoutLimits("DELETE FROM acars.POSITIONS WHERE (FLIGHT_ID=?)");
			_ps.setInt(1, flightID);
			executeUpdate(0);

			// Mark the flight as archived
			prepareStatementWithoutLimits("UPDATE acars.FLIGHTS SET ARCHIVED=?, PIREP=? WHERE (ID=?)");
			_ps.setBoolean(1, true);
			_ps.setBoolean(2, true);
			_ps.setInt(3, flightID);
			executeUpdate(0);
			
			// Commit the transaction
			commitTransaction();
		} catch (SQLException | IOException sie) {
			rollbackTransaction();
			throw new DAOException(sie);
		}
	}
}
