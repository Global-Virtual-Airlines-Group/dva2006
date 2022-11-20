// Copyright 2011, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.EnumDescription;

/**
 * An enumeration to list valid ACARS client types. 
 * @author Luke
 * @version 10.3
 * @since 4.1
 */

public enum ClientType implements EnumDescription {
	PILOT, DISPATCH, ATC, EVENT;

	@Override
	public String getDescription() {
		return (this == ATC) ? name() : EnumDescription.super.getDescription(); 
	}
}