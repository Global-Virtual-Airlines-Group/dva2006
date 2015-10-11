// Copyright 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import org.deltava.beans.acars.RouteEntry;
import org.deltava.dao.file.SetSerializedPosition;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write to the ACARS position archive.
 * @author Luke
 * @version 6.2
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
			startTransaction();
			
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

			// Write the serialized data
			if (positions.size() > 0) {
				prepareStatementWithoutLimits("REPLACE INTO acars.ARCHIVE (ID, CNT, ARCHIVED) VALUES (?, ?, NOW())");
				_ps.setInt(1, flightID);
				_ps.setInt(2, positions.size());
				executeUpdate(1);
				
				String hash = Integer.toHexString(flightID % 2048);
				File path = new File(SystemData.get("path.archive"), hash); path.mkdirs();
				File dt = new File(path, Integer.toHexString(flightID) + ".dat");
				
				// Write to the file system
				try (OutputStream os = new FileOutputStream(dt)) {
					try (OutputStream bos = new GZIPOutputStream(os, 8192)) {
						SetSerializedPosition psdao = new SetSerializedPosition(bos);
						psdao.archivePositions(flightID, positions);
					}
				}
			}
			
			commitTransaction();
		} catch (SQLException | IOException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Clears serialized data from the archive (assuming it has been persisted to disk).
	 * @param flightID the ACARS Flight ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clear(int flightID) throws DAOException {
		try {
			startTransaction();
			prepareStatementWithoutLimits("REPLACE INTO acars.ARCHIVE (SELECT ID, CNT, ARCHIVED FROM acars.POS_ARCHIVE WHERE (ID=?))");
			_ps.setInt(1, flightID);
			executeUpdate(0);
			prepareStatementWithoutLimits("UPDATE acars.POS_ARCHIVE SET CNT=? WHERE (ID=?)");
			_ps.setInt(1, 0);
			_ps.setInt(2, flightID);
			executeUpdate(0);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}