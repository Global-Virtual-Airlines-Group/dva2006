// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.ViewEntry;

/**
 * An enumeration of on-time values. 
 * @author Luke
 * @version 8.4
 * @since 8.4
 */

public enum OnTime implements ViewEntry {
	UNKNOWN(null), EARLY("sec"), ONTIME("pri"), LATE("warn");
	
	private final String _className;
	
	OnTime(String className) {
		_className = className;
	}

	@Override
	public String getRowClassName() {
		return _className;
	}
}