// Copyright 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * An enumeration of flight schedule sources.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum ScheduleSource implements org.deltava.beans.ComboAlias {
	DELTA("Delta PDF"), SKYTEAM("SkyTeam PDF"), INNOVATA("Innovata LLC"), LEGACY("Legacy Schedule"), MANUAL("Manual Entry");
	
	private final String _desc;
	
	ScheduleSource(String desc) {
		_desc = desc;
	}

	/**
	 * Returns the source description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}

	@Override
	public String getComboName() {
		return _desc;
	}
}