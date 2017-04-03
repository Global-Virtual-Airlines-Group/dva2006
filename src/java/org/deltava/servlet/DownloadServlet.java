// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import org.deltava.util.URLParser;

/**
 * A servlet to download attachment data.
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

abstract class DownloadServlet extends BasicAuthServlet {
	
	/**
	 * Interface for file type enumerations.
	 */
	protected interface FileType {
		
		/**
		 * The URL part to search for.
		 * @return the URL part
		 */
		String getURLPart();
	}
	
	/**
	 * A helper method to get the file type from the URL.
	 * @param up the URLParser
	 * @param values the possible types
	 * @return a FileType, or null if unknown
	 */
	protected static FileType getFileType(URLParser up, FileType[] values) {
		for (FileType t : values) {
			if (up.containsPath(t.getURLPart()))
				return t;
		}

		return null;
	}
}