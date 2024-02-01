// Copyright 2019, 2022, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration of Pilot statuses. 
 * @author Luke
 * @version 11.2
 * @since 9.0
 */

public enum PilotStatus implements ViewEntry, EnumDescription {
	ACTIVE(null), INACTIVE("opt2"), RETIRED("opt3"), TRANSFERRED("opt1"), SUSPENDED("err"), ONLEAVE("warn");
	
	private final String _viewCSS;
	
	PilotStatus(String css) {
		_viewCSS = css;
	}
	
	/**
	 * Returns if the Pilot is considered active.
	 * @return TRUE if Active or On Leave, otherwise FALSE
	 */
	public boolean isActive() {
		return (this == ACTIVE) || (this == ONLEAVE);
	}
	
	@Override
	public String getDescription() {
		return (this == ONLEAVE) ? "On Leave" : EnumDescription.super.getDescription(); 
	}

	@Override
	public String getRowClassName() {
		return _viewCSS;
	}
}