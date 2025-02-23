// Copyright 2019, 2020, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * An enumeration of flight schedule sources.
 * @author Luke
 * @version 11.5
 * @since 9.0
 */

public enum ScheduleSource implements org.deltava.beans.EnumDescription {
	DELTA("Delta PDF"), SKYTEAM("SkyTeam PDF"), INNOVATA("Innovata LLC"), LEGACY("Legacy Schedule"), MANUAL("Manual Entry"), CUSTOM("Custom Import"), VASYS("VASystems/PHPVMSv7"), DRAFT("Draft Flight Report");
	
	private final String _desc;
	
	ScheduleSource(String desc) {
		_desc = desc;
	}

	@Override
	public String getDescription() {
		return _desc;
	}
}