// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration of flight schedule sources.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum ScheduleSource implements ComboAlias {
	DELTA("Delta PDF", false), SKYTEAM("SkyTeam PDF", false), INNOVATA("Innovata LLC", true);
	
	private final String _desc;
	private final boolean _isInternal;
	
	ScheduleSource(String desc, boolean isInternal) {
		_desc = desc;
		_isInternal = isInternal;
	}

	/**
	 * Returns the source description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns if this is an internal data source.
	 * @return TRUE if internal, otherwise FALSE
	 */
	public boolean isInternal() {
		return _isInternal;
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