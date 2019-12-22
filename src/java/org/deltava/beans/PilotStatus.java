// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration of Pilot statuses. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum PilotStatus implements ViewEntry {
	ACTIVE("Active",null), INACTIVE("Inactive","opt2"), RETIRED("Retired","opt3"), TRANSFERRED("Transferred","opt1"), SUSPENDED("Suspended","err"), ONLEAVE("On Leave","warn");
	
	private final String _desc;
	private final String _viewCSS;
	
	PilotStatus(String desc, String css) {
		_desc = desc;
		_viewCSS = css;
	}
	
	/**
	 * Returns the status description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}

	@Override
	public String getRowClassName() {
		return _viewCSS;
	}
}