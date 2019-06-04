// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

/**
 * An enumeration of Transfer Request statuses.
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public enum TransferStatus {
	NEW("New"), PENDING("Pending Check Ride"), ASSIGNED("Check Ride Assigned"), COMPLETE("Complete");
	
	private final String _desc;
	
	TransferStatus(String desc) {
		_desc = desc;
	}
	
	/**
	 * Returns the status description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
}