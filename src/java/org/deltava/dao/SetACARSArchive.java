// Copyright 2012, 2015, 2016, 2019, 2023 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 11.1
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
	public void archive(int flightID, SequencedCollection<? extends RouteEntry> positions) throws DAOException {
		try {
			startTransaction();
			
			// Delete the existing flight data
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM acars.POSITIONS WHERE (FLIGHT_ID=?)")) {
				ps.setInt(1, flightID);
				executeUpdate(ps, 0);
			}
			
			// Delete the existing XACARS flight data
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM acars.POSITION_XARCHIVE WHERE (FLIGHT_ID=?)")) {
				ps.setInt(1, flightID);
				executeUpdate(ps, 0);
			}
			
			// Delete the ATC data
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM acars.POSITION_ATC WHERE (FLIGHT_ID=?)")) {
				ps.setInt(1, flightID);
				executeUpdate(ps, 0);
			}

			// Mark the flight as archived
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE acars.FLIGHTS SET ARCHIVED=?, PIREP=? WHERE (ID=?)")) {
				ps.setBoolean(1, true);
				ps.setBoolean(2, true);
				ps.setInt(3, flightID);
				executeUpdate(ps, 0);
			}

			// Write the serialized data
			if (positions.size() > 0) {
				ArchiveMetadata md = new ArchiveMetadata(flightID);
				byte[] data = null; CRC32 crc = new CRC32();
				try (ByteArrayOutputStream os = new ByteArrayOutputStream(10240)) {
					try (OutputStream bos = new GZIPOutputStream(os, 8192)) {
						SetSerializedPosition psdao = new SetSerializedPosition(bos);
						md.setFormat(psdao.archivePositions(flightID, positions));
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO acars.ARCHIVE (ID, CNT, SIZE, CRC, ARCHIVED, FMT) VALUES (?, ?, ?, ?, ?, ?)")) {
			ps.setInt(1, md.getID());
			ps.setInt(2, md.getPositionCount());
			ps.setInt(3, md.getSize());
			ps.setLong(4, md.getCRC32());
			ps.setTimestamp(5, createTimestamp(md.getArchivedOn()));
			ps.setInt(6, (md.getFormat() == null) ? -1 : md.getFormat().ordinal());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM acars.ARCHIVE WHERE (ID=?)")) {
			ps.setInt(1, flightID);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}