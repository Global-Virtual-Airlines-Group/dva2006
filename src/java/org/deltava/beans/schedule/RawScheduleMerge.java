// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * An enumeration of raw schedule merge options.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public enum RawScheduleMerge implements org.deltava.beans.EnumDescription {
	PURGE("Purge All Flights"), MERGE("Purge Imported Airlines"), APPEND("Append to existing Flights");
	
	private String _desc;
	
	RawScheduleMerge(String desc) {
		_desc = desc;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
}