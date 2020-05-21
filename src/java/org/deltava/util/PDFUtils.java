// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

/**
 * A utility class for Adobe PDF objects.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class PDFUtils {
	
	/**
	 * Adobe Portable Document Format magic number.
	 */
	public static final String PDF_MAGIC = "%PDF-";

	private PDFUtils() { // static class
		super();
	}
	
	/**
	 * Detects if a buffer contains a PDF document.
	 * @param data the buffer
	 * @return TRUE if the buffer starts with the magic number, otherwise FALSE  
	 */
	public static boolean isPDF(byte[] data) {
		if ((data == null) || (data.length < 16)) return false;

		byte[] mc = PDF_MAGIC.getBytes();
		for (int x = 0; x < mc.length; x++)
			if (data[x] != mc[x]) return false;
		
		return true;
	}
}