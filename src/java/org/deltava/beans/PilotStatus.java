// Copyright 2019, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration of Pilot statuses. 
 * @author Luke
 * @version 10.2
 * @since 9.0
 */

public enum PilotStatus implements ViewEntry, EnumDescription {
	ACTIVE(null), INACTIVE("opt2"), RETIRED("opt3"), TRANSFERRED("opt1"), SUSPENDED("err"), ONLEAVE("warn");
	
	private final String _viewCSS;
	
	PilotStatus(String css) {
		_viewCSS = css;
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