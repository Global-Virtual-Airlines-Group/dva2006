// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;

/**
 * A utility class for filesystem functions. 
 * @author Luke
 * @version 7.0
 * @since 4.2
 */

public class FileUtils {

	// static class
	private FileUtils() {
		super();
	}

	/**
	 * Returns the newest file in a directory.
	 * @param path the directory path
	 * @param ff a FilenameFilter to limit files selected
	 * @return the File, or null if empty
	 */
	public static File findNewest(String path, FilenameFilter ff) {
		File[] files = new File(path).listFiles(ff);
		if (files == null)
			return null;
		
		File newest = null;
		for (int x = 0; x < files.length; x++) {
			File f = files[x];
			if ((newest == null) || (f.lastModified() > newest.lastModified()))
				newest = f;
		}
		
		return newest;
	}

	/**
	 * Utility method to get filenames with a particular prefix and extension.
	 * @param prefix the prefix
	 * @param ext the extension
	 * @return a FilenameFilter
	 */
	public static FilenameFilter fileFilter(String prefix, String ext) {
		final String e = (ext == null) ? "" : ext.toLowerCase().replace("*", "");
		final String p = (prefix == null) ? "" : prefix.toLowerCase().replace("*", "");
		
		return new FilenameFilter() {
	        @Override
			public boolean accept(File dir, String name) {
	            String n = name.toLowerCase();
	            return n.startsWith(p) && n.endsWith(e);
	          }};
	}
}