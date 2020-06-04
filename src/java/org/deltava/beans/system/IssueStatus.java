// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.*;

/**
 * An enumeration of development issue statuses.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum IssueStatus implements EnumDescription {
	OPEN, FIXED, WORKEDAROUND, WONTFIX, DEFERRED, DUPLICATE;
	
	@Override
	public String getDescription() {
		switch (this) {
		case WORKEDAROUND:
			return "Worked Around";
		case WONTFIX:
			return "Won't Fix";
		default:
			return EnumDescription.super.getDescription();
		}
	}
}