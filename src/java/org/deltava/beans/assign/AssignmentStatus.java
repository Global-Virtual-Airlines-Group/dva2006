// Copyright 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.assign;

/**
 * An enumeration to store Flight Assignment statuses.
 * @author Luke
 * @version 9.0
 * @since 8.1
 */

public enum AssignmentStatus implements org.deltava.beans.EnumDescription {
	AVAILABLE, RESERVED, COMPLETE;
	
	/**
	 * Exception-safe name parser.
	 * @param name the status name
	 * @return an AssignmentStatus or null if unknown
	 */
	@Deprecated
	public static AssignmentStatus fromName(String name) {
		try {
			return valueOf(name.toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}
}