// Copyright 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import org.deltava.beans.FileType;
import org.deltava.util.URLParser;

/**
 * A servlet to download attachment data.
 * @author Luke
 * @version 10.6
 * @since 7.3
 */

abstract class DownloadServlet extends BasicAuthServlet {
	
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