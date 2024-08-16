// Copyright 2015, 2016, 2018, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.io.*;
import java.util.zip.CRC32;

import org.deltava.beans.Compression;
import org.deltava.beans.Helper;

import org.deltava.util.system.SystemData;

/**
 * A utility class to handle ACARS position archive hash buckets.
 * @author Luke
 * @version 11.2
 * @since 6.2
 */

@Helper(RouteEntry.class)
public class ArchiveHelper {
	
	private static final int BUCKETS = 2048;
	
	// static class
	private ArchiveHelper() {
		super();
	}
	
	/**
	 * Calculates the bucket for a given flight ID.
	 * @param id the flight ID
	 * @return the bucket name
	 */
	public static String getBucket(int id) {
		return Integer.toHexString(id % BUCKETS);
	}

	/**
	 * Returns the File containing archived ACARS position data.
	 * @param id the ACARS Flight ID
	 * @return a File, which may or may not exist
	 */
	public static File getPositions(int id) {
		File path = new File(SystemData.get("path.archive"), getBucket(id)); path.mkdirs();
		return new File(path, Integer.toHexString(id) + ".dat");
	}
	
	/**
	 * Returns the File containing archived route data.
	 * @param id the Flight ID
	 * @return a File, which may or may not exist
	 */
	public static File getRoute(int id) {
		File path = new File(SystemData.get("path.archive"), getBucket(id)); path.mkdirs();
		return new File(path, Integer.toHexString(id) + ".rte");
	}
	
	/**
	 * Returns the File containing archived online data.
	 * @param id the Flight ID
	 * @return a File, which may or may not exist
	 */
	public static File getOnline(int id) {
		File path = new File(SystemData.get("path.archive"), getBucket(id)); path.mkdirs();
		return new File(path, Integer.toHexString(id) + ".onl");
	}
	
	/**
	 * Loads and validates a Position Archive.
	 * @param md an ArchiveMetadata bean
	 * @return the loaded and uncompressed data
	 * @throws ArchiveValidationException if a validation error occurs
	 */
	public static byte[] load(ArchiveMetadata md) throws ArchiveValidationException {
		return load(md, null);
	}
	
	/**
	 * Loads and validates a Position Archive.
	 * @param md an ArchiveMetadata bean
	 * @param f the File to use, overriding the default for testing
	 * @return the loaded and uncompressed data
	 * @throws ArchiveValidationException if a validation error occurs
	 */
	public static byte[] load(ArchiveMetadata md, File f) throws ArchiveValidationException {
		
		if (md == null) return null;
		File aF = (f == null) ? getPositions(md.getID()) : f; 
		if (!aF.exists())
			throw new ArchiveValidationException(String.format("%s not found", aF.getAbsolutePath()));

		// Validate size
		if ((md.getSize() > 0) && (aF.length() != md.getSize()))
			throw new ArchiveValidationException(String.format("Invalid file size, expected %d, actual %d", Integer.valueOf(md.getSize()), Long.valueOf(aF.length())));
		
		// Load data and calculate checksum
		CRC32 crc = new CRC32(); byte[] rawData = null;
		try (InputStream in = new BufferedInputStream(new FileInputStream(aF), 16384); ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min((int)aF.length(), 65536))) {
			md.setCompression(Compression.detect(aF));
			int b = in.read();
			while (b != -1) {
				crc.update(b);
				out.write(b);
				b = in.read();
			}
				
			rawData = out.toByteArray();
		} catch (IOException ie) {
			throw new ArchiveValidationException(ie);
		}
		
		// Validate checksum
		if ((md.getCRC32() != 0) && (md.getCRC32() != crc.getValue()))
			throw new ArchiveValidationException(String.format("Invalid CRC32, expected %s, actual %s", Long.toHexString(md.getCRC32()), Long.toHexString(crc.getValue())));
		
		// Decompress data
		if (md.getCompression() != Compression.NONE) {
			try (InputStream in = md.getCompression().getStream(new ByteArrayInputStream(rawData)); ByteArrayOutputStream out = new ByteArrayOutputStream(rawData.length + 256)) {
				int b = in.read();	
				while (b != -1) {
					out.write(b);
					b = in.read();
				}
				
				rawData = out.toByteArray();
			} catch (IOException ie) {
				throw new ArchiveValidationException(ie);	
			}
		}
		
		// Peek ahead and load the format
		try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(rawData))) {
			short v = in.readShort();
			int flightID = in.readInt(); // flight ID
			int cnt = in.readInt();
			SerializedDataVersion ver = SerializedDataVersion.fromCode(v);
			if (ver == null)
				throw new ArchiveValidationException(String.format("Unknown Archive format - %d", Short.valueOf(v)));
			if (ver != md.getFormat())
				throw new ArchiveValidationException(String.format("Invalid Archive format, expected %s, actual %s", md.getFormat(), ver));
			if (flightID != md.getID())
				throw new ArchiveValidationException(String.format("Invalid Flight ID, expected %d, actual %d", Integer.valueOf(md.getID()), Integer.valueOf(flightID)));
			if (cnt != md.getPositionCount())
				throw new ArchiveValidationException(String.format("Invalid record count, expected %d, actual %d", Integer.valueOf(md.getPositionCount()), Integer.valueOf(cnt)));
		} catch (IOException ie) {
			throw new ArchiveValidationException(ie);
		}
		
		return rawData;
	}
}