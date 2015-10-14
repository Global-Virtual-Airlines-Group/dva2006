// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.io.*;

import org.deltava.beans.Helper;
import org.deltava.util.system.SystemData;

/**
 * A utility class to handle ACARS position archive hash buckets.
 * @author Luke
 * @version 6.2
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
	 * Returns the File containing archived ACARS flight data.
	 * @param id the ACARS Flight ID
	 * @return a File, which may or may not exist
	 */
	public static File getFile(int id) {
		
		String hash = Integer.toHexString(id % BUCKETS);
		File path = new File(SystemData.get("path.archive"), hash); path.mkdirs();
		return new File(path, Integer.toHexString(id) + ".dat");
	}
}