// Copyright 2015, 2016, 2018, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.io.*;

import org.deltava.beans.Helper;

import org.deltava.util.system.SystemData;

/**
 * A utility class to handle ACARS position archive hash buckets.
 * @author Luke
 * @version 10.5
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
	 * Returns the File containing archived ACARS position data.
	 * @param id the ACARS Flight ID
	 * @return a File, which may or may not exist
	 */
	public static File getPositions(int id) {
		String hash = Integer.toHexString(id % BUCKETS);
		File path = new File(SystemData.get("path.archive"), hash); path.mkdirs();
		return new File(path, Integer.toHexString(id) + ".dat");
	}
	
	/**
	 * Returns the File containing archived route data.
	 * @param id the Flight ID
	 * @return a File, which may or may not exist
	 */
	public static File getRoute(int id) {
		String hash = Integer.toHexString(id % BUCKETS);
		File path = new File(SystemData.get("path.archive"), hash); path.mkdirs();
		return new File(path, Integer.toHexString(id) + ".rte");
	}
	
	/**
	 * Returns the File containing archived online data.
	 * @param id the Flight ID
	 * @return a File, which may or may not exist
	 */
	public static File getOnline(int id) {
		String hash = Integer.toHexString(id % BUCKETS);
		File path = new File(SystemData.get("path.archive"), hash); path.mkdirs();
		return new File(path, Integer.toHexString(id) + ".onl");
	}
	
	/**
	 * Returns a buffered Input stream to a File.
	 * @param f the File
	 * @return an InputStream
	 * @throws IOException if an I/O error occurs
	 */
	public static InputStream getStream(File f) throws IOException {
		return new BufferedInputStream(new FileInputStream(f));
	}
}