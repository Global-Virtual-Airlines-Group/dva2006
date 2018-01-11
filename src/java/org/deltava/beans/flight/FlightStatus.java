// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.ViewEntry;

/**
 * An enumeration to store Flight Report states. 
 * @author Luke
 * @version 8.1
 * @since 8.1
 */

public enum FlightStatus implements ViewEntry {
	DRAFT("opt2"), SUBMITTED("opt1"), HOLD("warn"), OK(null), REJECTED("err");

	private final String _viewClass;
	
	/**
	 * Creates the enumeration value.
	 * @param viewClass the row CSS class name
	 */
	FlightStatus(String viewClass) {
		_viewClass = viewClass;
	}
	
	/**
	 * Returns the status description. 
	 * @return the description
	 */
	public String getDescription() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}

	@Override
	public String getRowClassName() {
		return _viewClass;
	}
}