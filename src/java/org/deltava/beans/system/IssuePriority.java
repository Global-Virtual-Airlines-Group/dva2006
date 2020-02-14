// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.*;

/**
 * An enumeration of development issue priorities. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum IssuePriority implements ComboAlias, EnumDescription {
	LOW, MEDIUM, HIGH, CRITICAL;
	
	@Override
	public String getComboAlias() {
		return name();
	}

	@Override
	public String getComboName() {
		return getDescription();
	}
}