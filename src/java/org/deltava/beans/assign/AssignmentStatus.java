// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.assign;

/**
 * An enumeration to store Flight Assignment statuses.
 * @author Luke
 * @version 8.1
 * @since 8.1
 */

public enum AssignmentStatus {
	AVAILABLE, RESERVED, COMPLETE;
	
	@Override
	public String toString() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}
	
	/**
	 * Exception-safe name parser.
	 * @param name the status name
	 * @return an AssignmentStatus or null if unknown
	 */
	public static AssignmentStatus fromName(String name) {
		try {
			return valueOf(name.toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}
}