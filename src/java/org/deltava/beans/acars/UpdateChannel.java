// Copyright 2015, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.EnumDescription;

/**
 * An enumeration to store ACARS update channels.  
 * @author Luke
 * @version 10.0
 * @since 6.1
 */

public enum UpdateChannel implements EnumDescription {
	RELEASE, BETA, RC;

	@Override
	public String getDescription() {
		return (this == RC) ? "Release Candidate" : EnumDescription.super.getDescription();
	}
}