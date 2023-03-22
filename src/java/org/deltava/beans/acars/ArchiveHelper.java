// Copyright 2015, 2016, 2018, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
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
}