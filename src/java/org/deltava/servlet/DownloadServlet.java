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
	
	protected interface FileType {
	
		String getURLPart();
	}
	
	/**
	 * A helper method to get the image type from the URL.
	 * @param up the URLParser
	 * @return a FileType, or null if unknown
	 */
	protected static FileType getFileType(URLParser up) {
		for (FileType t : FileType.values()) {
			if (up.containsPath(t.getURLPart()))
				return t;
		}

		return null;
	}
}