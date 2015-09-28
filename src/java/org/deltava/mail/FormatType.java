// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.mail;

/**
 * An enumeration to store formatting options for types whose formatting
 * cannot be reliably inferred.
 * @author Luke
 * @version 6.2
 * @since 6.2
 */

enum FormatType {
	RAW("$!"), DATE("$D"), TIME("$T"), DATETIME("$DT"), DISTANCE("$L");
	
	private final String _code;
	
	FormatType(String code) {
		_code = code;
	}
	
	/**
	 * Determines the formatting type from the name and its suffix.
	 * @param objName the object name
	 * @return the FormatType
	 */
	public static FormatType getType(String objName) {
		for (FormatType ft : values()) {
			if (objName.endsWith(ft._code))
				return ft;
		}
		
		return RAW;
	}
}