// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

/**
 * An enumeration of Document Library types.
 * @author Luke
 * @version 8.4
 * @since 8.4
 */

public enum DocumentType {
	UNKNOWN("Unknown"), PDF("Acrobat Document"), XLS("Excel Worksheet");
	
	private final String _desc;
	
	DocumentType(String desc) {
		_desc = desc;
	}
	
	/**
	 * Returns the type description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Exception-safe enumeration parser.
	 * @param fileName the file name
	 * @return a DocumentType, or UNKNOWN
	 */
	public static DocumentType fromFilename(String fileName) {
		try {
			return valueOf(fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase());
		} catch (Exception e) {
			return UNKNOWN;
		}
	}
}