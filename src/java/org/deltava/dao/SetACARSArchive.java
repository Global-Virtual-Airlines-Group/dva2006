// Copyright 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.io.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.zip.*;

import org.deltava.beans.acars.*;

import org.deltava.dao.file.SetSerializedPosition;

/**
 * A Data Access Object to write to the ACARS position archive.
 * @author Luke
 * @version 7.2
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
	 * @param positions a Collection of RouteEntry beans 
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
				ArchiveMetadata md = new ArchiveMetadata(flightID);
				byte[] data = null; CRC32 crc = new CRC32();
				try (ByteArrayOutputStream os = new ByteArrayOutputStream(10240)) {
					try (OutputStream bos = new GZIPOutputStream(os, 8192)) {
						SetSerializedPosition psdao = new SetSerializedPosition(bos);
						psdao.archivePositions(flightID, positions);
						data = os.toByteArray();
					}
					
					crc.update(data);
					md.setArchivedOn(Instant.now());
					md.setCRC32(crc.getValue());
					md.setPositionCount(positions.size());
					md.setSize(data.length);
				}
				
				update(md);
				
				// Write to the file system
				try (OutputStream os = new BufferedOutputStream(new FileOutputStream(ArchiveHelper.getPositions(flightID)), 8192)) {
					os.write(data);
				}
			}
			
			commitTransaction();
		} catch (SQLException | IOException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates Position Archive metadata.
	 * @param md an ArchiveMetadata bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(ArchiveMetadata md) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO acars.ARCHIVE (ID, CNT, SIZE, CRC, ARCHIVED) VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, md.getID());
			_ps.setInt(2, md.getPositionCount());
			_ps.setInt(3, md.getSize());
			_ps.setLong(4, md.getCRC32());
			_ps.setTimestamp(5, createTimestamp(md.getArchivedOn()));
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes Position Archive metadata.
	 * @param flightID the flight ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int flightID) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM acars.ARCHIVE WHERE (ID=?)");
			_ps.setInt(1, flightID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}