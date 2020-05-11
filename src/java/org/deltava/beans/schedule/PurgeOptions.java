// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.*;

/**
 * An enumeration to store Flight Schedule purge options.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum PurgeOptions implements EnumDescription {
	NONE("Do Not Purge"), EXISTING("Existing Sources"), ALL("Complete Schedule");

	private final String _desc;

	PurgeOptions(String desc) {
		_desc = desc;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
}