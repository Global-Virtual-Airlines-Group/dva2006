// Copyright 2018, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.*;

/**
 * An enumeration of on-time values. 
 * @author Luke
 * @version 10.3
 * @since 8.4
 */

public enum OnTime implements ViewEntry, EnumDescription {
	UNKNOWN(null), EARLY("sec"), ONTIME("pri"), LATE("warn");
	
	private final String _className;
	
	OnTime(String className) {
		_className = className;
	}
	
	@Override
	public String getDescription() {
		return (this == ONTIME) ? "On Time" : EnumDescription.super.getDescription();
	}

	@Override
	public String getRowClassName() {
		return _className;
	}
}