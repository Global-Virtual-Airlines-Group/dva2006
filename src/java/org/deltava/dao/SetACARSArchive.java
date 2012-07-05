// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.deltava.beans.acars.RouteEntry;
import org.deltava.dao.file.SetSerializedPosition;

/**
 * A Data Access Object to write to the ACARS position archive.
 * @author Luke
 * @version 4.2
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
			ByteArrayOutputStream out = new ByteArrayOutputStream(20480);
			SetSerializedPosition psdao = new SetSerializedPosition(out);
			psdao.archivePositions(flightID, positions);
			startTransaction();
			
			// Write serialized data
			if (positions.size() > 0) {
				prepareStatementWithoutLimits("REPLACE INTO acars.POS_ARCHIVE (ID, CNT, ARCHIVED, DATA) "
					+ "VALUES (?, ?, NOW(), ?)");
				_ps.setInt(1, flightID);
				_ps.setInt(2, positions.size());
				_ps.setBytes(3, out.toByteArray());
				executeUpdate(1);
			}
			
			// Delete the existing flight data
			prepareStatementWithoutLimits("DELETE FROM acars.POSITIONS WHERE (FLIGHT_ID=?)");
			_ps.setInt(1, flightID);
			executeUpdate(0);
			
			// Delete the existing XACARS flight data
			prepareStatementWithoutLimits("DELETE FROM acars.POSITION_XARCHIVE WHERE (FLIGHT_ID=?)");
			_ps.setInt(1, flightID);
			executeUpdate(0);
			
			// Delete the ATC data
			prepareStatementWithoutLimits("DELETE FROM acars.POSITION_ATC WHERE (FLIGHT_ID=?)");
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
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}